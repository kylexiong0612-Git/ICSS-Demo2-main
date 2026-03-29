# 前端开发规范-PC端

## 1 编码规范

### 1.1 命名规范

* 文件命名：组件文件使用大驼峰命名法（如：UserInfo.vue）

* 组件命名：组件名使用大驼峰命名法（如：UserInfo）

* 变量命名：使用小驼峰命名法（如：userInfo）

* 常量命名：使用全大写加下划线分隔（如：USER\_INFO）

### 1.2 CSS 规范

* 使用 Sass 预处理器

* 组件样式使用 scoped 属性隔离

* 类名使用短横线分隔（如：user-info）

### 1.3 Vue 组件规范

* 组件结构按照 template、script、style 的顺序排列

* 组件中使用到的 props 需要明确定义类型和默认值

* 组件方法按照功能相关性分组，优先级：生命周期方法 > data/watch/computed > 自定义方法

* 组件中尽量避免使用内联样式

### 1.4 接口调用规范

* 所有接口统一在 \[src/api/]目录下定义

* 接口方法命名采用动词+名词形式（如：getUserInfo）

* 接口调用统一使用 \[src/util/request.js]封装的 axios

### 1.5 数据规范

* 所有筛选项的绑定值应放在同一个对象中，便于管理和传递

* 选择器的枚举值应在data对象中定义，便于管理

### 1.6 注释规范

* 组件应包含基本的功能说明注释

* 复杂业务逻辑应添加详细注释

* 方法应包含参数说明和返回值说明

## 2.UI规范

### 2.1 UI框架介绍

UI框架使用了[Element UI](https://element.eleme.cn/#/zh-CN)定制版本，可直接按照官方文档使用elementUI组件，还引入了chaos-ui，在elementUI的基础上封装了一些常用的组件,如:面包屑导航（chaos-breadcrumb）、描述列表（chaos-description）、异常处理（ChaosException）、筛选项（chaos-filter）、标题组件（chaos-title）等。

### 2.2 UI布局规范

页面基本布局按照如下代码,在公共样式文件src/assets/style/common.scss中对于页面布局已有定义，因此class命名需严格按照如下名称。

```vue
<template>
  <div class="main-container">
    <div class="main-breadcrumb">{{ $t('menu.serviceOrderSearch') }}</div>
    <div class="main-area">
      <div class="main-content main-pd32 main-grey-border">
        {{ $t('menu.serviceOrderSearch') }}
      </div>
    </div>
  </div>
</template>
```

* 其中.main-breadcrumb是面包屑组件区域，应使用chaos-breadcrumb组件，使用示例如2.3 组件使用说明章节

* .main-content为主要内容区域，以下内容包含在它之中

* 筛选项使用chaos-filter组件，使用示例如2.3 组件使用说明章节

* 通过CSS确保所有筛选项控件宽度保填充筛选项空间

* 表格区域使用el-table组件，small尺寸，使用示例如Element UI官方文档

* 筛选项和表格区域之间使用el-divider分割，分割线颜色#dfe3ea，上间距4px,下间距16px

* 操作按钮使用使用el-button组件置于表格上方，与表格保持12px的间距，左对齐，按钮之间保持8px的间距

* 分页区域使用el-pagination组件，默认展示10条数据，包含当前页、每页条数、总条数，支持页码切换、每页显示条数调整、跳转指定页功能在，无背景色, 使用示例如Element UI官方文档，应放置在表格下方并右对齐，组件本身保持12px的上下内边距

### 2.3 组件使用说明

#### 2.3.1 ChaosBasedataList 基础数据列表

##### 普通选择器

用于从基础数据中选择值，支持单选、多选和过滤功能。

###### 基础用法

```html
<!-- 基础用法 -->
<chaos-basedata-list
  v-model="value"
  :baseUrl="baseUrl"
  dataType="BASE_UW_CLIENT_TASK_STATUS"
  keyField="TASK_STATUS"
  labelField="CN_NAME"
/>

<!-- 使用filter过滤选项值 -->
<chaos-basedata-list
  v-model="value1"
  :baseUrl="baseUrl"
  dataType="BASE_UW_CLIENT_TASK_STATUS"
  :filter="filterMethod"
  keyField="TASK_STATUS"
  labelField="CN_NAME"
/>

<!-- 多选 -->
<chaos-basedata-list
  v-model="value2"
  :baseUrl="baseUrl"
  dataType="BASE_UW_CLIENT_TASK_STATUS"
  keyField="TASK_STATUS"
  labelField="CN_NAME"
  multiple
  collapse-tags
/>
```

```javascript
export default {
  data () {
    return {
      value: '',
      value1: '',
      value2: [],
      baseUrl: window.__g_config.VUE_APP_ENV === 'LOCAL' ? '' : window.__g_config.VUE_APP_BASE_URL
    }
  },
  methods: {
    filterMethod (item) {
      return ['001', '002', '003'].indexOf(item.TASK_STATUS) >= 0
    }
  }
}
```

###### Props

| 参数              | 说明                       | 类型             | 默认值    | 可选值                                                                                                                                       |
| --------------- | ------------------------ | -------------- | ------ | ----------------------------------------------------------------------------------------------------------------------------------------- |
| v-model         | 绑定值                      | String / Array | --     | --                                                                                                                                        |
| autoload        | 是否在组件渲染时就触发查询接口          | Boolean        | true   | <br />                                                                                                                                    |
| baseUrl         | 域名                       | String         | <br /> | 注意区分prd/di/dev环境                                                                                                                          |
| dataType        | 查询的数据表名                  | String         | <br /> | <br />                                                                                                                                    |
| keyField        | 作为选项key的字段名              | String         | <br /> | --                                                                                                                                        |
| labelField      | 作为选项标签的字段名               | String         | <br /> | --                                                                                                                                        |
| extraFields     | 选项标签的其他字段                | Array          | \[ ]   | --                                                                                                                                        |
| placeholder     | 占位符                      | String         | 请选择    | --                                                                                                                                        |
| filter          | 过滤选项值的方法                 | Function       | <br /> | <br />                                                                                                                                    |
| queryConditions | 查询条件                     | Array          | <br /> | 格式为\[{key: "BUSINESS\_SOURCE",value: "valueExample",matchType: "fullMatch"}]，matchType的可选值为fullMatch-全匹配/fuzzyMatch-模糊匹配/nullMatch-null匹配 |
| orderConditions | （v0.0.34新增）排序条件          | Array          | <br /> | 格式为\[{key: "BUSINESS\_SOURCE",value: "desc"}]，value的可选值为desc/asc                                                                          |
| multiple        | 是否多选                     | Boolean        | false  | <br />                                                                                                                                    |
| collapse-tags   | 多选时是否将选中值按文字的形式展示        | Boolean        | false  | <br />                                                                                                                                    |
| multiple-limit  | 多选时用户最多可以选择的项目数，为 0 则不限制 | Number         | 0      | <br />                                                                                                                                    |
| size            | 尺寸                       | String         | 请选择    | small/mini                                                                                                                                |
| disabled        | 是否禁用                     | Boolean        | false  | <br />                                                                                                                                    |
| clearable       | 是否可以清空值                  | Boolean        | true   | <br />                                                                                                                                    |

###### Events

| name   | 说明         | 回调参数             |
| ------ | ---------- | ---------------- |
| change | 选中值发生变化时触发 | (当前的选中值,当前选中的对象) |

###### Methods

| 方法名          | 说明     | 参数 |
| ------------ | ------ | -- |
| search       | 查询选项列表 | 无  |
| clear        | 清空选中值  | 无  |
| clearOptions | 清空选项列表 | 无  |
| getList      | 获取选项列表 | 无  |

##### 级联选择器

用于选择具有层级关系的数据，如省市区、职业分类等。

```html
<!-- 这里的componentType必须为'cascaderSelect' -->
<chaos-basedata-list
  ref="demo"
  v-model="value"
  :baseUrl="baseUrl"
  componentType="cascaderSelect"
  :dataTypeList="dataTypeList"
  :multiple="false"
  :collapse-tags="true"
/>
```

```javascript
export default {
  data () {
    return {
      value: null,
      // 有几级就要传几个，按顺序对应
      dataTypeList: [{
        // 一级表名
        dataType: 'BASE_OCCUPATION_CATEGORY',
        // 一级表key字段
        keyField: 'OCCUPATION_CATEGORY',
        // 一级表label字段
        labelField: 'OCCUPATION_CATEGORY_NAME',
      }, {
        // 二级表名
        dataType: 'BASE_OCCUPATION_CLASS',
        // 二级表key字段
        keyField: 'OCCUPATION_CLASS',
        // 二级表label字段
        labelField: 'OCCUPATION_CLASS_NAME',
        // 二级表parent字段
        parentField: 'OCCUPATION_CATEGORY',
      }, {
        // 三级表名
        dataType: 'BASE_OCCUPATION',
        // 三级表key字段
        keyField: 'OCCUPATION_CODE',
        // 三级表label字段
        labelField: 'OCCUPATION_NAME',
        // 三级表parent字段
        parentField: 'OCCUPATION_CLASS',
      }]
    }
  }
}
```

###### Props

| 参数            | 说明                | 类型             | 默认值                 | 可选值                             |
| ------------- | ----------------- | -------------- | ------------------- | ------------------------------- |
| componentType | 组件类型              | String / Array | 必须传"cascaderSelect" | cascaderSelect                  |
| v-model       | 绑定值               | String / Array | --                  | --                              |
| baseUrl       | 域名                | String         | <br />              | 注意区分prd/di/dev环境                |
| placeholder   | 占位符               | String         | 请选择                 | --                              |
| dataTypeList  | 查询的数据表名List       | Array          | <br />              | 参考示例代码，v0.0.34新增orderConditions |
| multiple      | 是否多选              | Boolean        | false               | <br />                          |
| collapse-tags | 多选时是否将选中值按文字的形式展示 | Boolean        | false               | <br />                          |
| size          | 尺寸                | String         | 请选择                 | small/mini                      |
| disabled      | 是否禁用              | Boolean        | false               | <br />                          |
| clearable     | 是否可以清空值           | Boolean        | true                | <br />                          |

###### Events

| name   | 说明         | 回调参数             |
| ------ | ---------- | ---------------- |
| change | 选中值发生变化时触发 | (当前的选中值,当前选中的对象) |

###### Methods

| 方法名             | 说明        | 参数 |
| --------------- | --------- | -- |
| reloadOption    | 重新渲染选项值列表 | 无  |
| getSelectedData | 获取选中的完整对象 | 无  |

##### 树形选择器

用于选择树形结构数据，如组织机构、部门等。

```html
<!-- 这里的componentType必须为'branchTree' -->
<div style="width:400px;">
  <chaos-basedata-list
    ref="branchTree"
    v-model="value1"
    componentType="branchTree"
    :baseUrl="baseUrl"
    :multiple="false"
    :disabled="false"
    :disabledIds="['86480001']"
    :clearable="false"
    labelField="BRANCH_NAME"
    :orderConditions="orderConditions"
    size="small"
    @select="selectMethod"
    @input="inputMethod"
  />
</div>

<!-- 这里的componentType必须为'ouTree' -->
<div style="width:400px;">
  <chaos-basedata-list
    v-model="value2"
    componentType="ouTree"
    :baseUrl="baseUrl"
    :tenantOpenId="tenantOpenId"
    :multiple="true"
    :multipleLimit="4"
    :collapseTags="true"
    placeholder="请选择机构"
    size="small"
    @select="selectMethod"
    @input="inputMethod"
  />
</div>
```

```javascript
export default {
  data () {
    return {
      value1: null,
      value2: null,
      // 当前租户openId，必传
      tenantOpenId: '8dfdffc9386b457a981e322bc7d02c4c',
      baseUrl: window.__g_config.VUE_APP_ENV === 'LOCAL' ? '' : window.__g_config.VUE_APP_BASE_URL,
      orderConditions: [{
        key: 'BRANCH_CODE',
        value: 'asc'
      }]
    }
  },
  created () {
    // 模拟异步请求
    setTimeout(() => {
      this.value1 = '86'
      // 改变v-model绑定值时，要手动调用initValue方法，否则因为树的数据为懒加载，可能会出现值无法匹配而导致回显问题
      this.$refs.branchTree.initValue()
    }, 500)
  },
  methods: {
    selectMethod (node) {
      console.log(node)
    },
    inputMethod (value) {
      console.log(value)
    }
  }
}
```

###### Props

| 参数              | 说明                       | 类型             | 默认值                       | 可选值                                                                                         |
| --------------- | ------------------------ | -------------- | ------------------------- | ------------------------------------------------------------------------------------------- |
| componentType   | 组件类型                     | String / Array | 必须传"branchTree"或者"ouTree" | branchTree/ouTree                                                                           |
| v-model         | 绑定值                      | String / Array | --                        | --                                                                                          |
| tenantOpenId    | 租户openId                 | String         | 在componentType为ouTree时，必传 | --                                                                                          |
| baseUrl         | 域名                       | String         | <br />                    | 注意区分prd/di/dev环境                                                                            |
| placeholder     | 占位符                      | String         | 请选择                       | --                                                                                          |
| dataTypeList    | 查询的数据表名List              | Array          | <br />                    | 参考示例代码                                                                                      |
| multiple        | 是否多选                     | Boolean        | false                     | <br />                                                                                      |
| collapse-tags   | 多选时是否将选中值按文字的形式展示        | Boolean        | false                     | <br />                                                                                      |
| multiple-limit  | 多选时用户最多可以选择的项目数，为 0 则不限制 | Number         | 0                         | <br />                                                                                      |
| size            | 尺寸                       | String         | 请选择                       | small/mini                                                                                  |
| disabledIds     | 禁止选中的节点                  | Array          | \[]                       | <br />                                                                                      |
| disabled        | 是否禁用                     | Boolean        | false                     | <br />                                                                                      |
| clearable       | 是否可以清空值                  | Boolean        | true                      | <br />                                                                                      |
| orderConditions | （v0.0.34新增）排序条件          | Array          | <br />                    | 只有在componentType为branchTree时有效，格式为\[{key: "BRANCH\_CODE",value: "desc"}]，value的可选值为desc/asc |
| labelField      | （v0.0.34新增）显示的节点名称字段     | String         | BRANCH\_FULL\_NAME        | 只有在componentType为branchTree时有效，可选值为BRANCH\_FULL\_NAME/BRANCH\_NAME                          |

###### Events

| name   | 说明        | 回调参数   |
| ------ | --------- | ------ |
| select | 选择一个选项后触发 | 当前node |
| input  | 值更改后触发    | value  |

###### Methods

| 方法名       | 说明         | 参数 |
| --------- | ---------- | -- |
| initValue | 初始化value回显 | 无  |

##### 人员树选择器

用于在组织架构中选择人员。

```html
<el-button type="primary" style="width:120px;margin:20px 0" @click="openDialog">选择人员</el-button>
<el-dialog
  title="选择人员"
  :visible.sync="dialogVisible"
>
  <!-- 这里的componentType必须为'ouPeopleTree' -->
  <!-- 需要自己定义宽度和高度 -->
  <chaos-basedata-list
    v-if="dialogVisible"
    v-model="value"
    style="width:100%;height:500px;"
    componentType="ouPeopleTree"
    :baseUrl="baseUrl"
    :tenantOpenId="tenantOpenId"
    :disabledIds="disabledIds"
    :multiple="true"
    :multipleLimit="10"
    @confirm="confirmMethod"
    @cancel="cancelMethod"
  />
</el-dialog>
```

```javascript
export default {
  data () {
    return {
      dialogVisible: false,
      value: null,
      disabledIds: ['0317a748db424eaf934cf9336ab2c4cc', '975e789658d74a0db8de456597ad3715', '7c9efb08a6c148a3aa962021e75a8a0d', '8151b16ce3a240babf7407e8fe2228e1', '20e9277ea6f8413b91a221a5b3506a4f'],
      // 当前租户openId，必传
      tenantOpenId: '8dfdffc9386b457a981e322bc7d02c4c',
      baseUrl: window.__g_config.VUE_APP_ENV === 'LOCAL' ? '' : window.__g_config.VUE_APP_BASE_URL
    }
  },
  methods: {
    openDialog () {
      this.dialogVisible = true
    },
    confirmMethod (val) {
      console.log(val)
      this.dialogVisible = false
    },
    cancelMethod (val) {
      console.log(val)
      this.dialogVisible = false
    },
  }
}
```

###### Props

| 参数             | 说明                       | 类型             | 默认值                             | 可选值              |
| -------------- | ------------------------ | -------------- | ------------------------------- | ---------------- |
| componentType  | 组件类型                     | String / Array | 必须传"ouPeopleTree"               | ouPeopleTree     |
| v-model        | 绑定值                      | String / Array | --                              | --               |
| tenantOpenId   | 租户openId                 | String         | 在componentType为ouPeopleTree时，必传 | --               |
| baseUrl        | 域名                       | String         | <br />                          | 注意区分prd/di/dev环境 |
| multiple       | 是否多选                     | Boolean        | false                           | <br />           |
| multiple-limit | 多选时用户最多可以选择的项目数，为 0 则不限制 | Number         | 0                               | <br />           |
| disabledIds    | 禁用的人员tenantUserOpenId    | Array          | \[]                             | <br />           |

###### Events

| name    | 说明         | 回调参数      |
| ------- | ---------- | --------- |
| confirm | 点击【确定】按钮触发 | 选中的人员信息   |
| cancel  | 点击【取消】按钮触发 | 初始传入的人员信息 |

##### 表单验证

支持表单验证功能。

```html
<el-form ref="form" :model="form" label-width="80px" :rules="rules">
  <el-form-item label="机构" prop="value1">
    <chaos-basedata-list
      v-model="form.value1"
      componentType="ouTree"
      :baseUrl="baseUrl"
      :tenantOpenId="tenantOpenId"
      :multiple="false"
      placeholder="请选择机构"
      size="small"
      @input="validateMethod"
    />
  </el-form-item>
</el-form>
```

#### 2.3.2 ChaosBreadcrumb 面包屑导航

用于显示当前页面的路径导航，并支持返回上一页功能。

```html
<template>
  <chaos-breadcrumb showBack :backMethod="backMethod"/>
</template>
```

```javascript
export default {
  methods: {
    backMethod () {
      console.log('back')
    }
  }
}
```

##### Props

| 参数         | 说明           | 类型       | 默认值                              | 可选值 |
| ---------- | ------------ | -------- | -------------------------------- | --- |
| showBack   | 是否显示返回按钮     | Boolean  | false                            | --  |
| backMethod | 自定义的返回按钮点击事件 | Function | 不传时，默认的返回事件是this.\$router.go(-1) | --  |

#### 2.3.3 ChaosDescription 描述列表

用于展示只读信息的列表，支持有边框和无边框两种样式。

##### 有边框样式

```html
<chaos-description :column="3" :border="true" title="有边框" style="margin-bottom:30px;">
  <chaos-description-item label="1级标题" :span="1">1级内容1级内容</chaos-description-item>
  <chaos-description-item label="2级标题:" :span="1"><el-input v-model="input" size="small" /></chaos-description-item>
  <chaos-description-item label="3级标题" :span="1">
    <el-button type="text" size="small">保存</el-button>
    <el-button type="text" size="small">编辑</el-button>
  </chaos-description-item>
  <chaos-description-item label="4级标题:" :span="1">4级内容</chaos-description-item>
  <chaos-description-item label="5级标题:" :span="2">5级内容</chaos-description-item>
  <chaos-description-item label="6级标题:" :span="1" required>6级内容</chaos-description-item>
</chaos-description>
```

##### 无边框样式

```html
<chaos-description :column="3" :border="false" title="无边框">
  <chaos-description-item label="1级标题1级标题:" :span="1">1级内容1级内容</chaos-description-item>
  <chaos-description-item label="2级标题:" :span="1">1级内容1级内容</chaos-description-item>
  <chaos-description-item label="3级标题:" :span="1">3级内容</chaos-description-item>
  <chaos-description-item label="4级标题:" :span="1">4级内容</chaos-description-item>
  <chaos-description-item label="5级标题:" :span="1">5级内容</chaos-description-item>
  <chaos-description-item label="6级标题:" :span="1" required>6级内容</chaos-description-item>
</chaos-description>
```

##### Description Props

| 参数         | 说明             | 类型      | 默认值   | 可选值        |
| ---------- | -------------- | ------- | ----- | ---------- |
| title      | 描述列表的标题，显示在最顶部 | String  | --    | --         |
| border     | 是否展示边框         | Boolean | false | true/false |
| column     | 每行最多展示项        | Number  | 3     | <br />     |
| labelWidth | 有边框时，描述项标签的宽度  | Number  | 100   | --         |

##### Description Item Props

| 参数       | 说明                     | 类型      | 默认值    | 可选值        |
| -------- | ---------------------- | ------- | ------ | ---------- |
| label    | 描述项标签文字                | String  | <br /> | --         |
| span     | 合并列（包含列的数量），不能大于column | Number  | 1      | --         |
| required | 是否必须项，即是否显示红色\*号       | Boolean | false  | true/false |

##### Description Item Slots

| name  | 说明    |
| ----- | ----- |
| label | 自定义标签 |

#### ChaosException 异常处理

用于展示异常信息，通常在请求失败时调用。

```javascript
this.$chaosException({
  message: '投保失败',
  majorCode: '2001',
  detailCode: '20003'
})
```

#### 2.3.4 ChaosFilter 筛选项

用于展示查询表格上方筛选项的组件。

```html
<template>
  <chaos-filter :submitDisabled="true" @submit="submit" @clear="clear">
    <chaos-filter-item label="查询条件2：">
      <el-input v-model="input2" size="small" />
    </chaos-filter-item>
    <chaos-filter-item label="查询条件3：">
      <el-input v-model="input3" size="small" />
    </chaos-filter-item>
  </chaos-filter>
</template>
```

##### Filter Props

| 参数             | 说明             | 类型      | 默认值   | 可选值        |
| -------------- | -------------- | ------- | ----- | ---------- |
| size           | 【查询】和【清空】按钮的尺寸 | String  | small | --         |
| submitDisabled | 【查询】按钮禁用状态     | Boolean | false | true/false |
| submitLoading  | 【查询】按钮加载状态     | Boolean | false | true/false |

##### Events

| name   | 说明          |
| ------ | ----------- |
| submit | 点击【查询】按钮时触发 |
| clear  | 点击【清空】按钮时触发 |

##### Filter Item Props

| 参数       | 说明                | 类型      | 默认值    | 可选值        |
| -------- | ----------------- | ------- | ------ | ---------- |
| label    | 筛选项标签文字           | String  | <br /> | --         |
| span     | 合并列（包含列的数量），不能大于4 | Number  | 1      | --         |
| required | 是否必填项，即是否显示红色\*号  | Boolean | false  | true/false |

##### Filter Item Slots

| name  | 说明    |
| ----- | ----- |
| label | 自定义标签 |

#### 2.3.5 ChaosSelect 选择器

增强版的选择器组件，支持自定义选项显示和多种配置。

```html
<template>
  <chaos-select
    v-model="selectValue"
    :options="options"
    :multiple="true"
    :normalizer="normalizer"
    :disabled="false"
    :clearable="false"
    :searchable="true"
    @open="openMethod"
    @close="closeMethod"
    @change="changeMethod"
  >
    <span slot="value" slot-scope="{node}" style="display: flex;align-items: center;">
      <span>{{ node.label }}-{{ node.raw.phone }}-value</span>
    </span>
    <span slot="label" slot-scope="{node}">{{ node.label }}-{{ node.raw.phone }}</span>
  </chaos-select>
</template>
```

```javascript
export default {
  data () {
    return {
      selectValue: null,
      options: [{
        id: 'fruits',
        name: 'Fruits',
        phone: '18322223333'
      }, {
        id: 'vegetables',
        name: 'Vegetables',
        phone: '18322223333'
      }, {
        id: 'fruits2',
        name: 'Fruits2',
        phone: '18322223333'
      }, {
        id: 'fruits3',
        name: 'Fruits3',
        phone: '18322223333'
      }],
      normalizer (node) {
        return {
          id: node.id,
          label: node.name,
          phone: node.phone,
          isDisabled: node.id === 'fruits3'
        }
      }
    }
  },
  methods: {
    openMethod () {
      console.log('open it')
    },
    closeMethod (val) {
      console.log('close it', val)
    },
    changeMethod (val) {
      console.log('change', val)
    }
  }
}
```

##### Props

| 参数             | 说明                 | 类型             | 默认值          | 可选值 |
| -------------- | ------------------ | -------------- | ------------ | --- |
| v-model        | 绑定值                | String / Array | --           | --  |
| multiple       | 是否多选               | Boolean        | true         | --  |
| options        | 可用选项的数组            | Array          | --           | --  |
| disabled       | 是否禁用               | Boolean        | false        | --  |
| normalizer     | 用于规范源数据            | Function       | node => node | --  |
| clearable      | 是否可以清空选项           | Boolean        | false        | --  |
| searchable     | 是否可以搜索             | Boolean        | false        | --  |
| placeholder    | 占位符                | String         | 请选择          | --  |
| no-match-text  | 搜索条件无匹配时显示的文字      | String         | 无匹配数据        | --  |
| no-data-text   | 选项为空时显示的文字         | String         | 无数据          | --  |
| append-to-body | 是否将下拉菜单插入至 body 元素 | Boolean        | false        | --  |

##### Slots

| name   | 说明      | props  |
| ------ | ------- | ------ |
| option | 自定义选项标签 | {node} |
| label  | 自定义值标签  | {node} |

##### Events

| name     | 说明          | 回调参数     |
| -------- | ----------- | -------- |
| open     | 菜单打开时触发     | --       |
| close    | 菜单关闭时触发     | value    |
| change   | 选中值变化时触发    | value    |
| select   | 选中一个选项时触发   | 规范化后的选项值 |
| deselect | 取消选中一个选项时触发 | 规范化后的选项值 |

#### 2.3.6 ChaosTitle 标题组件

用于展示页面标题。

```html
<template>
  <chaos-title/>
  <!-- 或 -->
  <chaos-title title="自定义的title"/>
</template>
```

##### Props

| 参数    | 说明      | 类型     | 默认值               | 可选值 |
| ----- | ------- | ------ | ----------------- | --- |
| title | 自定义标题文本 | String | 不设置则为当前路由的title属性 | --  |

