<template>
  <a-card
    :body-style="{padding: '24px 32px'}"
    :bordered="false">
    <a-form
      @submit="handleSubmit"
      :form="form">
      <a-form-item
        label="Project Name"
        :label-col="{lg: {span: 5}, sm: {span: 7}}"
        :wrapper-col="{lg: {span: 16}, sm: {span: 17} }">
        <a-input
          type="text"
          placeholder="the project name"
          v-decorator="['name',{ rules: [{ required: true, message: 'Project Name is required'} ]}]" />
      </a-form-item>

      <a-form-item
        label="Project Type"
        :label-col="{lg: {span: 5}, sm: {span: 7}}"
        :wrapper-col="{lg: {span: 16}, sm: {span: 17} }">
        <a-select
          show-search
          option-filter-prop="children"
          :filter-option="filterOption"
          placeholder="the project type"
          ref="types"
          @change="handleType"
          v-decorator="[ 'type', {rules: [{ required: true, message: 'Project Type is required'}]} ]">
          <a-select-option
            v-for="p in options.types"
            :disabled="p.id === 2"
            :key="p.id"
            :value="p.id">
            {{ p.name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item
        label="CVS"
        :label-col="{lg: {span: 5}, sm: {span: 7}}"
        :wrapper-col="{lg: {span: 16}, sm: {span: 17} }">
        <a-select
          show-search
          option-filter-prop="children"
          :filter-option="filterOption"
          placeholder="CVS"
          ref="repository"
          @change="handleResp"
          v-decorator="[ 'repository', {rules: [{ required: true, message: 'CVS is required'}]} ]">
          <a-select-option
            v-for="p in options.repository"
            :disabled="p.id === 2"
            :key="p.id"
            :value="p.id">
            {{ p.name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item
        label="Repository URL"
        :label-col="{lg: {span: 5}, sm: {span: 7}}"
        :wrapper-col="{lg: {span: 16}, sm: {span: 17} }">
        <a-input
          type="text"
          :addon-before="schema"
          placeholder="The Repository URL for this project"
          @change="handleSchema"
          v-decorator="['url',{ rules: [{ required: true, message: 'Repository URL is required'} ]}]" />
      </a-form-item>

      <a-form-item
        label="Branches"
        :label-col="{lg: {span: 5}, sm: {span: 7}}"
        :wrapper-col="{lg: {span: 16}, sm: {span: 17} }">
        <a-input
          type="text"
          placeholder="Branches of the this project"
          default-value="main"
          v-decorator="['branches',{ rules: [{ required: true } ],initialValue:'main'}]" />
      </a-form-item>

      <a-form-item
        label="UserName"
        :label-col="{lg: {span: 5}, sm: {span: 7}}"
        :wrapper-col="{lg: {span: 16}, sm: {span: 17} }">
        <a-input
          type="text"
          placeholder="UserName for this project"
          v-decorator="['username']" />
      </a-form-item>

      <a-form-item
        label="Password"
        :label-col="{lg: {span: 5}, sm: {span: 7}}"
        :wrapper-col="{lg: {span: 16}, sm: {span: 17} }">
        <a-input
          type="password"
          placeholder="Password for this project"
          v-decorator="['password']" />
      </a-form-item>

      <a-form-item
        label="POM"
        :label-col="{lg: {span: 5}, sm: {span: 7}}"
        :wrapper-col="{lg: {span: 16}, sm: {span: 17} }">
        <a-input
          type="text"
          placeholder="By default,lookup pom.xml in root path,You can manually specify the module to compile pom.xml"
          v-decorator="['pom',{ rules: [{ message: 'Specifies the module to compile pom.xml If it is not specified, it is found under the root path pom.xml' } ]}]" />
      </a-form-item>

      <a-form-item
        label="Description"
        :label-col="{lg: {span: 5}, sm: {span: 7}}"
        :wrapper-col="{lg: {span: 16}, sm: {span: 17} }">
        <a-textarea
          rows="4"
          name="description"
          placeholder="Description for this project"
          v-decorator="['description']" />
      </a-form-item>

      <a-form-item
        :wrapper-col="{ span: 24 }"
        style="text-align: center">
        <a-button
          @click="handleGoBack">
          Back
        </a-button>
        <a-button
          html-type="submit"
          type="primary"
          style="margin-left: 15px">
          Submit
        </a-button>
      </a-form-item>
    </a-form>
  </a-card>
</template>

<script>

import { create} from '@api/project'

export default {
  name: 'BaseForm',
  data () {
    return {
      schema: 'ssh',
      options: {
        repository: [
          { id: 1, name: 'GitHub/GitLab', default: true },
          { id: 2, name: 'Subversion', default: false }
        ],
        types: [
          {id: 1, name: 'apache flink',default: true },
          {id: 2, name: 'apache spark',default: false }
        ]
      }
    }
  },
  mounted () {
    this.select()
  },
  beforeMount () {
    this.form = this.$form.createForm(this)
  },
  methods: {

    filterOption (input, option) {
      return option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
    },

    handleResp (selected) {
      this.repository = selected
    },

    handleType (selected) {
      this.types = selected
    },

    handleSchema () {
      console.log(this.url)
    },
    // handler
    handleSubmit: function (e) {
      e.preventDefault()
      this.form.validateFields((err, values) => {
        if (!err) {
          create({
            name: values.name,
            url: values.url,
            repository: values.repository,
            type: values.type,
            branches: values.branches,
            username: values.username,
            password: values.password,
            pom: values.pom,
            description: values.description
          }).then((resp) => {
            const created = resp.data
            if (created) {
              this.$router.push({ path: '/flink/project' })
            } else {
              this.$notification.error({
                message: 'Project save failed',
                description: resp['message']
              })
            }
          }).catch((error) => {
            this.$message.error(error.message)
          })
        }
      })
    },

    handleGoBack () {
      this.$router.go(-1)
    }
  }
}
</script>

<style scoped>
.ant-list-item-meta-description {
  margin-left: 20px;
}

.ant-list-item-content {
  margin-right: 20px;
}

.conf_item {
  margin-bottom: 0px;
}

.conf-desc {
  color: darkgrey;
  margin-bottom: 0px
}

.conf-switch {
  color: darkgrey;
  margin-left: 5px;
}

.ant-input-number {
  width: 100%;
}

.ant-form-explain {
  margin-top: -5px;
}
</style>
