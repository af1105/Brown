<template>
  <a-modal v-model='visible' title='点位配置' :width='850' :closable='false' :maskClosable='false'
           :bodyStyle='{paddingTop:"15px"}'>
    <template slot='footer'>
      <a-button key='back' @click='handleClose'>关闭</a-button>
      <a-button key='submit' type='primary' @click='handleOk' v-show='!disableEdit'>保存</a-button>
    </template>
    <component ref='PointModel' :is='resourceComponent' v-if='resourceComponent' :disable-edit='disableEdit'></component>
  </a-modal>
</template>

<script>
import Vue from 'vue'

export default {
  name: 'PointsConfigModel',
  data() {
    return {
      visible: false,
      resourceType: null,
      resourceMode: null, // source or dest
      resourceIndex: -1,
      disableEdit: false,
      resourceComponent: undefined,
      resourceComponentMap: {
        OPCUA: 'OpcUAPointsModel',
        SNMP: 'SnmpPointsModel',
        MODBUSTCP: 'ModbusTcpPointsModel'
      }
    }
  },
  methods: {
    show(resourceType, points) {
      this.init(resourceType, null, -1, points, true)
    },
    config(resourceType, resourceMode, resourceIndex, points) {
      this.init(resourceType, resourceMode, resourceIndex, points, false)
    },
    init(resourceType, resourceMode, resourceIndex, points, disableEdit) {
      this.visible = true
      this.resourceType = resourceType
      this.resourceMode = resourceMode
      this.resourceIndex = resourceIndex
      this.disableEdit = disableEdit
      if (resourceType && this.resourceComponentMap[resourceType]) {
        import('./model/' + this.resourceComponentMap[resourceType] + '.vue')
          .then(component => {
            this.resourceComponent = Vue.extend(component.default)
            this.$nextTick(() => {
              this.$refs.PointModel.set(points)
            })
          })
      }
    },
    handleOk() {
      this.$emit('update', this.resourceMode, this.resourceIndex, this.$refs.PointModel.get())
      this.handleClose()
    },
    handleClose() {
      this.visible = false
      this.resourceType = null
      this.resourceMode = null // source or dest
      this.resourceIndex = -1
    }
  }
}
</script>

<style scoped>

</style>