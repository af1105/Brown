<template>
  <a-modal
    :confirmLoading='confirmLoading'
    :title='resourceMode==="source"?"数据源":"目标资源"'
    :dialog-style="{ top: '50px' }"
    :width='600'
    :visible='visible'
    @cancel='close'
    :destroyOnClose='true'
    :bodyStyle='{maxHeight:"650px",overflowY:"auto",paddingBottom:"0"}'
  >
    <a-form-model ref='ruleForm' :model='modal' layout='vertical' :rules='rules'>
      <a-form-model-item label='类型' prop='resourceType'>
        <a-select v-model='modal.resourceType' placeholder='请选择资源类型' @change='changeResourceType'
                  :disabled='resourceIndex >= 0' show-search>
          <a-select-opt-group v-for='(group,groupIndex) in resourceTypeList' :key='groupIndex' :label='group.group'>
            <a-select-option v-for='(item,itemIndex) in group.list' :value='item.code' :key='itemIndex'>{{ item.name
              }}
            </a-select-option>
          </a-select-opt-group>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label='资源' prop='resourceId' v-show='modal.resourceType'>
        <a-select v-model='modal.resourceId' placeholder='请选择资源' @change='changeResource'>
          <a-select-option v-for='(item,index) in resourceList' :value='item.resourceId' :key='index'>
            {{ item.resourceName }}
          </a-select-option>
        </a-select>
      </a-form-model-item>
      <component ref='PropertiesModal' :is='resourceComponent' v-if='resourceComponent'
                 :type='resourceMode'></component>
    </a-form-model>
    <div class='bottom-button'>
      <a-button :style="{ marginRight: '8px' }" @click='close'> 取消</a-button>
      <a-button type='primary' @click='handleOk'> 确定</a-button>
    </div>
  </a-modal>
</template>

<script>
import { postAction } from '@/api/manage'
import { resourceComponentMap, getResourceListByType } from '@/config/resource.config'
import Vue from 'vue'

export default {
  name: 'ResourceModel',
  data() {
    return {
      title: '操作',
      visible: false,
      confirmLoading: false,
      modal: {},
      url: {
        resourceList: '/api/resource/list'
      },
      rules: {
        resourceType: [{ required: true, message: '请选择资源类型', trigger: 'change' }],
        resourceId: [{ required: true, message: '请选择资源', trigger: 'change' }]
      },
      resourceList: [],
      resourceTypeList: [],
      resourceMode: null, // source or dest
      resourceIndex: -1,
      resourceComponent: undefined,
      resourceComponentMap
    }
  }
  ,
  methods: {
    add(resourceMode) {
      this.visible = true
      this.resourceMode = resourceMode
      this.resourceIndex = -1
      this.modal = {}
      this.resourceTypeList = getResourceListByType(resourceMode)
    },
    edit(resourceMode, record, resourceIndex) {
      this.visible = true
      this.resourceMode = resourceMode
      this.resourceIndex = resourceIndex
      this.modal = Object.assign({}, record)
      this.resourceTypeList = getResourceListByType(resourceMode)
      this.listResource(record.resourceType)
      this.importComponent(record.resourceType, record.properties)
    },
    changeResourceType(resourceType) {
      this.modal = { resourceType: resourceType }
      this.listResource(resourceType)
      this.importComponent(resourceType)
    },
    importComponent(resourceType, properties) {
      import('../properties/' + this.resourceComponentMap[resourceType] + '.vue').then(component => {
        this.resourceComponent = Vue.extend(component.default)
        if (properties) {
          this.$nextTick(() => {
            this.$refs.PropertiesModal.set(properties)
          })
        }
      })
    },
    close() {
      this.resourceList = []
      this.resourceTypeList = []
      this.resourceMode = null // source or dest
      this.resourceIndex = -1
      this.resourceComponent = undefined
      this.visible = false
    },
    handleOk() {
      const that = this
      this.$refs.ruleForm.validate(valid => {
        if (!valid) return false
        that.$refs.PropertiesModal.get((checked, prop) => {
          if (!checked) return false
          that.confirmLoading = true
          that.modal.properties = prop
          if (that.resourceIndex >= 0) {
            that.$emit('update', this.resourceMode, this.modal, this.resourceIndex)
          } else {
            that.$emit('add', this.resourceMode, this.modal)
          }
          that.confirmLoading = false
          that.close()
        })
      })
    },
    listResource(type) {
      postAction(this.url.resourceList, { resourceType: type }).then(res => {
        this.resourceList = res.data
      })
    },
    changeResource(resourceId) {
      let tempRuntimeId = this.modal.resourceRuntimeId
      let index = this.resourceList.findIndex(resource => resource.resourceId === resourceId)
      this.modal = Object.assign({}, this.resourceList[index])
      this.modal.resourceRuntimeId = tempRuntimeId
      this.$nextTick(() => {
        if (this.modal.resourceType) {
          this.$refs.PropertiesModal.set(this.modal.properties)
        }
      })
    },
    filterOption(input, option) {
      if (!option.componentOptions.children[0].text) {
        return false
      }
      return option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
    }
  }
}
</script>

<style scoped>


</style>
