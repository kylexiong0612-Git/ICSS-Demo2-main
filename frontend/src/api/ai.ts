import axios from 'axios'
import type { Message } from '@/types'

// API Key 由 Vite 开发代理在服务端注入（vite.config.ts proxy），不暴露于前端 bundle。
// 生产环境需在 BFF 层持有 ZHIPU_API_KEY 并代理 /api/ai 路径。
const MODEL = 'glm-4-flash'

const zhipuClient = axios.create({
  baseURL: '/api/ai',
  timeout: 30000,
})

interface ZhipuChoice {
  message: { role: string; content: string }
  finish_reason: string
}

interface ZhipuResponse {
  choices: ZhipuChoice[]
  usage?: { total_tokens: number }
}

interface TaskAnalysis {
  summary: string
  suggestion: string
  tags: string[]
}

async function zhipuRequest(
  messages: { role: string; content: string }[],
  jsonMode = false,
): Promise<string> {
  const { data } = await zhipuClient.post<ZhipuResponse>('/chat/completions', {
    model: MODEL,
    messages,
    ...(jsonMode ? { response_format: { type: 'json_object' } } : {}),
  })

  if (!Array.isArray(data.choices) || data.choices.length === 0) {
    throw new Error('[AI] 接口响应异常：choices 字段为空')
  }
  const content = data.choices[0].message?.content
  if (typeof content !== 'string') {
    throw new Error('[AI] 接口响应异常：content 字段类型错误')
  }
  return content
}

export async function getBotResponse(userMessage: string, history: Message[]): Promise<string> {
  const chatHistory = history.filter(
    (m, i) => !(i === history.length - 1 && m.role === 'user' && m.content === userMessage),
  )

  const messages = [
    {
      role: 'system',
      content:
        "你是一个专业的人寿保险智能助理'宏小二'。你的目标是帮助用户解答保险产品条款、运营规则、投保和理赔流程。如果用户要求转人工，或者问题过于复杂，请礼貌地引导用户转接人工服务。不要提供虚假的生产数据。",
    },
    ...chatHistory.map(m => ({
      role: m.role === 'user' ? 'user' : 'assistant',
      content: m.content,
    })),
    { role: 'user', content: userMessage },
  ]

  try {
    return (await zhipuRequest(messages)) || '抱歉，我暂时无法回答。'
  } catch (err) {
    console.error('[AI] getBotResponse 失败:', err)
    return '抱歉，我暂时无法回答。'
  }
}

export async function preProcessTask(history: Message[]): Promise<TaskAnalysis> {
  const prompt = `聊天记录如下：\n${history.map(m => `${m.role}: ${m.content}`).join('\n')}`

  const messages = [
    {
      role: 'system',
      content:
        '你是一个保险坐席辅助AI。请分析用户与机器人的聊天记录，生成：1. 核心诉求摘要；2. 处理建议；3. 任务标签（如：理赔咨询、保单变更、投诉倾向）。请严格以JSON格式返回，格式为：{"summary": "...", "suggestion": "...", "tags": ["..."]}',
    },
    { role: 'user', content: prompt },
  ]

  try {
    const text = await zhipuRequest(messages, true)
    const jsonMatch = text.match(/\{[\s\S]*\}/)
    const parsed: unknown = JSON.parse(jsonMatch?.[0] || '{}')
    if (typeof parsed !== 'object' || parsed === null) throw new Error('非法 JSON 结构')
    const p = parsed as Record<string, unknown>
    return {
      summary: typeof p.summary === 'string' ? p.summary : '无法解析摘要',
      suggestion: typeof p.suggestion === 'string' ? p.suggestion : '请人工核实诉求',
      tags: Array.isArray(p.tags)
        ? p.tags.filter((t): t is string => typeof t === 'string')
        : ['通用咨询'],
    }
  } catch {
    return { summary: '无法解析摘要', suggestion: '请人工核实诉求', tags: ['通用咨询'] }
  }
}
