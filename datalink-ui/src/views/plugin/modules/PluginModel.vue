<template>
  <a-modal
    :confirmLoading='confirmLoading'
    title='插件'
    :width='600'
    :visible='visible'
    @cancel='onClose'
    :destroyOnClose='true'
  >
    <a-form-model ref='ruleForm' :model='modal' layout='vertical' :rules='rules'>
      <a-form-model-item label='名称' prop='pluginName'>
        <a-input v-model='modal.pluginName' placeholder='请上传插件文件' :read-only='true'></a-input>
      </a-form-model-item>
      <a-form-model-item label='包路径' prop='packagePath'>
        <a-input v-model='modal.packagePath' placeholder='请输入包路径'></a-input>
      </a-form-model-item>
      <a-form-model-item label='说明'>
        <a-input v-model='modal.description' placeholder='请输入说明'></a-input>
      </a-form-model-item>
    </a-form-model>
    <a-form-item label='文件上传'>
      <div :key='Math.random()'>
        <a-upload
          :showUploadList='false'
          :beforeUpload='(file) => checkFile(file,100,["jar"])'
          @change='handlerUpload'
          :customRequest='customRequest'
        >
          <a-button>
            <a-icon type='upload' />
            选择文件
          </a-button>
        </a-upload>
      </div>
    </a-form-item>

    <div class='bottom-button'>
      <a-button :style="{ marginRight: '8px' }" @click='onClose'> 取消</a-button>
      <a-button type='primary' @click='handleOk'> 确定</a-button>
    </div>
  </a-modal>
</template>

<script>
import { checkFile } from '@/utils/util'
import { postAction, putAction, uploadAction } from '@/api/manage'

export default {
  name: 'PluginModel',
  data() {
    return {
      title: '操作',
      visible: false,
      confirmLoading: false,
      modal: {
        pluginName: ''
      },
      url: {
        add: '/api/plugin/add',
        update: '/api/plugin/update'
      },
      rules: {
        pluginName: [{ required: true, message: '请输入插件名称', trigger: 'change' }],
        packagePath: [{ required: true, message: '请输入包路径', trigger: 'blur' }]
      },
      isEdit: false,
    }
  },
  methods: {
    checkFile,
    add() {
      this.edit({})
    },
    edit(record) {
      this.modal = Object.assign({}, this.modal, record)
      this.isEdit = !!record.key
      this.visible = true
    }
    ,
    onClose() {
      this.modal = { pluginName: '' }
      this.visible = false
    }
    ,
    handleOk() {
      const that = this
      this.$refs.ruleForm.validate(valid => {
        if (valid) {
          that.confirmLoading = true
          let obj
          if (this.isEdit) {
            obj = putAction(this.url.update, this.modal)
          } else {
            obj = postAction(this.url.add, this.modal)
          }
          obj.then(res => {
            if (res.code === 200) {
              that.$message.success('保存成功')
              that.$emit('ok')
            } else {
              that.$message.error('保存失败')
            }
          })
            .finally(() => {
              that.confirmLoading = false
              that.onClose()
            })
        } else {
          return false
        }
      })
    },
    filterOption(input, option) {
      if (!option.componentOptions.children[0].text) {
        return false
      }
      return option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
    },
    handlerUpload(info) {
      if (info.file.status !== 'uploading') {
      }
      if (info.file.status === 'done') {
        if (info.file.response.code === 200) {
          this.modal.pluginName = info.file.response.data.name
          this.$message.success(`${info.file.name} 上传成功`)
        } else {
          this.$message.error(`上传出错,${info.file.response.message}`)
        }
      } else if (info.file.status === 'error') {
        this.$message.error(`上传出错`)
      }
    },
    customRequest(options) {
      let params = new FormData()
      params.append('file', options.file)
      uploadAction('/api/plugin/upload', params).then((res) => {
        options.onSuccess(res, options.file)
      })
    }

  }
}
</script>

<style scoped>


</style>
