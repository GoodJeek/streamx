<template>
  <a-popover
    v-model="visible"
    trigger="click"
    placement="bottomRight"
    overlay-class-name="header-notice-wrapper"
    :auto-adjust-overflow="true"
    :arrow-point-at-center="true"
    :overlay-style="{ width: '300px', top: '50px' }">
    <template
      slot="content">
      <a-spin
        :spinning="loadding">
        <a-tabs v-if="1===2">
          <a-tab-pane
            tab="通知"
            key="1">
            <a-list>
              <a-list-item>
                <a-list-item-meta
                  title="告警"
                  description="2020-11-12 07:00:00">
                  <a-avatar
                    style="background-color: white"
                    slot="avatar"
                    src="https://gw.alipayobjects.com/zos/rmsportal/ThXAXghbEsBCCSDihZxY.png" />
                </a-list-item-meta>
              </a-list-item>
            </a-list>
          </a-tab-pane>
          <a-tab-pane
            tab="消息"
            key="2">
            123
          </a-tab-pane>
          <a-tab-pane
            tab="待办"
            key="3">
            123
          </a-tab-pane>
        </a-tabs>
      </a-spin>
    </template>
    <span
      @click="fetchNotice"
      class="header-notice">
      <a-badge
        count="0">
        <a-icon
          style="font-size: 16px; padding: 4px"
          type="bell" />
      </a-badge>
    </span>
  </a-popover>
</template>

<script>
export default {
  name: 'HeaderNotice',
  data () {
    return {
      loadding: false,
      visible: false
    }
  },
  methods: {
    fetchNotice () {
      if (!this.visible) {
        this.loadding = true
        setTimeout(() => {
          this.loadding = false
        }, 2000)
      } else {
        this.loadding = false
      }
      this.visible = !this.visible
    }
  }
}
</script>

<style lang="css">
  .header-notice-wrapper {
    top: 50px !important;
  }
</style>
<style lang="less" scoped>
  .header-notice{
    display: inline-block;
    transition: all 0.3s;

    span {
      vertical-align: initial;
    }
  }
</style>
