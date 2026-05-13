<template>
  <el-dialog
    :model-value="modelValue"
    title="生产故障报障"
    width="480px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" size="small">
      <el-form-item label="任务编号">
        <el-input :value="task?.id ?? '—'" disabled />
      </el-form-item>
      <el-form-item label="客户姓名">
        <el-input :value="customerName || '—'" disabled />
      </el-form-item>
      <el-form-item label="故障类型">
        <el-select v-model="form.faultType" placeholder="请选择故障类型" clearable style="width: 100%">
          <el-option label="系统宕机" value="系统宕机" />
          <el-option label="数据异常" value="数据异常" />
          <el-option label="接口故障" value="接口故障" />
          <el-option label="性能问题" value="性能问题" />
          <el-option label="其他" value="其他" />
        </el-select>
      </el-form-item>
      <el-form-item label="故障描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请详细描述故障现象..."
        />
      </el-form-item>
      <el-form-item label="影响范围">
        <el-input v-model="form.affectedScope" placeholder="例如：江苏地区所有客户" />
      </el-form-item>
      <el-form-item label="紧急程度" prop="urgency">
        <el-select v-model="form.urgency" placeholder="请选择紧急程度" style="width: 100%">
          <el-option label="低" value="Low" />
          <el-option label="中" value="Medium" />
          <el-option label="高" value="High" />
          <el-option label="紧急" value="Urgent" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="danger" @click="handleSubmit">提交报障</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { ServiceTask } from '@/types'
import { useTaskStore } from '@/stores/taskStore'

const props = defineProps<{
  modelValue: boolean
  task: ServiceTask | null
  customerName: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submitted: [taskId: string]
}>()

const taskStore = useTaskStore()
const formRef = ref<FormInstance>()

const form = reactive({
  faultType: '',
  description: '',
  affectedScope: '',
  urgency: '' as 'Low' | 'Medium' | 'High' | 'Urgent' | '',
})

const rules: FormRules = {
  description: [{ required: true, message: '请填写故障描述', trigger: 'blur' }],
  urgency: [{ required: true, message: '请选择紧急程度', trigger: 'change' }],
}

function handleClose() {
  emit('update:modelValue', false)
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    const task = await taskStore.submitFaultTask({
      relatedTaskId: props.task?.id,
      faultType: form.faultType || undefined,
      description: form.description,
      affectedScope: form.affectedScope || undefined,
      urgency: form.urgency as 'Low' | 'Medium' | 'High' | 'Urgent',
    })
    emit('submitted', task.id)
    emit('update:modelValue', false)
    resetForm()
  } catch {
    // validation failed
  }
}

function resetForm() {
  form.faultType = ''
  form.description = ''
  form.affectedScope = ''
  form.urgency = ''
  formRef.value?.clearValidate()
}
</script>
