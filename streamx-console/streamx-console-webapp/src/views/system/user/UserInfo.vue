<template>
  <a-modal
    v-model="show"
    :centered="true"
    :keyboard="false"
    :footer="null"
    :width="750"
    @cancel="handleCancleClick"
    title="用户信息">
    <a-layout
      class="user-info">
      <a-layout-sider
        class="user-info-side">
        <a-avatar
          shape="square"
          :size="115"
          icon="user"
          :src="`static/avatar/${data.avatar}`" />
      </a-layout-sider>
      <a-layout-content
        class="user-content-one">
        <p>
          <a-icon
            type="user" />账户：{{ data.username }}
        </p>
        <p
          :title="data.roleName">
          <a-icon
            type="star" />角色：{{ data.roleName? data.roleName: '暂无角色' }}
        </p>
        <p>
          <a-icon
            type="skin" />性别：{{ sex }}
        </p>
        <p>
          <a-icon
            type="phone" />电话：{{ data.mobile ? data.mobile : '暂未绑定电话' }}
        </p>
        <p>
          <a-icon
            type="mail" />邮箱：{{ data.email ? data.email : '暂未绑定邮箱' }}
        </p>
      </a-layout-content>
      <a-layout-content
        class="user-content-two">
        <p>
          <a-icon
            type="smile"
            v-if="data.status === '1'" />
          <a-icon
            type="frown"
            v-else />状态：
          <template
            v-if="data.status === '0'">
            <a-tag
              color="red">
              锁定
            </a-tag>
          </template>
          <template
            v-else-if="data.status === '1'">
            <a-tag
              color="cyan">
              有效
            </a-tag>
          </template>
          <template
            v-else>
            {{ data.status }}
          </template>
        </p>
        <p>
          <a-icon
            type="clock-circle" />创建时间：{{ data.createTime }}
        </p>
        <p>
          <a-icon
            type="login" />最近登录：{{ data.lastLoginTime }}
        </p>
        <p
          :title="data.description">
          <a-icon
            type="message" />描述：{{ data.description }}
        </p>
      </a-layout-content>
    </a-layout>
  </a-modal>
</template>
<script>
export default {
  name: 'UserInfo',
  props: {
    visible: {
      type: Boolean,
      require: true,
      default: false
    },
    data: {
      type: Object,
      default: () => ({}),
      require: true
    }
  },
  computed: {
    show: {
      get: function () {
        return this.visible
      },
      set: function () {
      }
    },
    sex () {
      switch (this.data.sex) {
        case '0':
          return '男'
        case '1':
          return '女'
        case '2':
          return '保密'
        default:
          return this.data.sex
      }
    }
  },
  methods: {
    handleCancleClick () {
      this.$emit('close')
    }
  }
}
</script>
<style lang="less" scoped>
  .user-info {
    background: #fff;
    padding: 0 10px 10px 10px;
  }
  .user-info-side {
    background: #fff;
  }
  .user-info-side {
    max-width: 10rem !important;
    min-width: 10rem !important;
    width: 10rem !important;
  }
  .user-content-one{
    margin-right: 1.2rem;
  }
  p {
    margin-bottom: 1rem;
    max-width: 15.5rem;
  }
  i {
    margin-right: .8rem;
  }
</style>
