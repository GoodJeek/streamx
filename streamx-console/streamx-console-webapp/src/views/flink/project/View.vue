<template>
  <div>
    <a-card
      style="margin-top: 24px"
      :bordered="false">
      <div slot="extra">
        <a-radio-group
          button-style="solid"
          default-value="">
          <a-radio-button
            @click="handleQuery()"
            value="">
            All
          </a-radio-button>
          <a-radio-button
            @click="handleQuery(-1)"
            value="-1">
            Unbuild
          </a-radio-button>
          <a-radio-button
            @click="handleQuery(0)"
            value="0">
            Building
          </a-radio-button>
          <a-radio-button
            @click="handleQuery(1)"
            value="1">
            Build Success
          </a-radio-button>
          <a-radio-button
            @click="handleQuery(2)"
            value="2">
            Build Failed
          </a-radio-button>
        </a-radio-group>
        <a-input-search
          @search="handleSearch"
          style="margin-left: 16px; width: 272px;" />
      </div>

      <div
        class="operate"
        v-permit="'project:create'">
        <a-button
          type="dashed"
          style="width: 100%"
          icon="plus"
          @click="handleAdd">
          Add New
        </a-button>
      </div>

      <a-list
        size="large"
        :pagination="pagination">
        <a-list-item
          :key="index"
          v-for="(item, index) in dataSource">
          <a-list-item-meta>
            <svg-icon class="avatar" v-if="item.type === 1" name="flink" size="large" slot="avatar"></svg-icon>
            <svg-icon class="avatar" v-if="item.type === 2" name="spark" size="large" slot="avatar"></svg-icon>
            <a slot="title">
              {{ item.name }}
              <a-badge
                status="processing"
                title="installing"
                v-if="item.buildState === 0" />
            </a>
            <a-popover
              arrow-point-at-center
              trigger="hover"
              slot="description">
              <template slot="content">
                {{ item.url }}
              </template>
              <a-button
                style="border:unset;height:20px;background:unset;margin-left:0;padding-left:0px;">
                <ellipsis
                  :length="controller.ellipsis">
                  {{ item.description }}
                </ellipsis>
              </a-button>
            </a-popover>
          </a-list-item-meta>

          <div class="list-content">
            <div
              class="list-content-item">
              <span>CVS</span>
              <p>
                <a-icon
                  type="github"
                  two-tone-color />
              </p>
            </div>
            <div class="list-content-item">
              <span>Branches</span>
              <p>
                <a-tag
                  color="blue">
                  {{ item.branches }}
                </a-tag>
              </p>
            </div>
            <div
              class="list-content-item"
              style="width: 180px">
              <span>Last Build</span>
              <p v-if="item.lastBuild">
                {{ item.lastBuild }}
              </p>
              <p v-else>
                --
              </p>
            </div>
            <div
              class="list-content-item"
              style="width: 150px">
              <span>Build State</span>
              <p v-if="item.buildState == 2">
                <a-tag color="#f5222d">FAILED</a-tag>
              </p>
              <p v-else>
                <a-tag color="#52c41a">SUCCESSFUL</a-tag>
              </p>
            </div>
          </div>

          <div class="operation">
            <a-icon
              v-if="item.buildState === 0"
              type="sync"
              style="color:#4a9ff5"
              spin
              @click="handleSeeLog(item)" />
            <a-popconfirm
              v-else
              v-permit="'project:build'"
              title="Are you sure build this project?"
              cancel-text="No"
              ok-text="Yes"
              @confirm="handleBuild(item)">
              <a-icon
                type="thunderbolt"
                theme="twoTone"
                two-tone-color="#4a9ff5" />
            </a-popconfirm>

            <a-icon
              type="edit"
              v-permit="'project:update'"
              theme="twoTone"
              two-tone-color="#4a9ff5"
              style="width:30px;" />
            <a-icon
              type="delete"
              v-permit="'project:delete'"
              theme="twoTone"
              two-tone-color="#4a9ff5"
              style="width:30px;"
              @click="handleRemove(item)"
              />
          </div>
        </a-list-item>
      </a-list>
    </a-card>

    <a-modal
      v-model="controller.visible"
      width="65%"
      :body-style="controller.modalStyle"
      :destroy-on-close="controller.modalDestroyOnClose"
      :footer="null"
      @ok="handleClose">
      <template slot="title">
        <a-icon type="code" />&nbsp;
        {{ controller.consoleName }}
      </template>
      <div
        id="terminal"
        ref="terminal"
        class="terminal" />
    </a-modal>
  </div>
</template>
<script>
import { build, list, remove } from '@api/project'
import { check } from '@api/setting'
import Ellipsis from '@comp/Ellipsis'
import SockJS from 'sockjs-client'
import Stomp from 'webstomp-client'
import { Terminal } from 'xterm'
import 'xterm/css/xterm.css'

import SvgIcon from '@/components/SvgIcon'
import {baseUrl} from '@/api/baseUrl'

export default {
  components: { Ellipsis, SvgIcon },
  data () {
    return {
      loading: false,
      advanced: false,
      dataSource: [],
      selectedRowKeys: [],
      queryParams: {},
      sortedInfo: null,
      stompClient: null,
      terminal: null,
      controller: {
        ellipsis: 100,
        modalStyle: {
          height: '600px',
          padding: '5px'
        },
        visible: false,
        modalDestroyOnClose: true,
        consoleName: null
      },
      pagination: {
        pageSizeOptions: ['10', '20', '30', '40', '100'],
        defaultCurrent: 1,
        defaultPageSize: 10,
        showQuickJumper: true,
        showSizeChanger: true,
        showTotal: (total, range) => `display ${range[0]} ~ ${range[1]} record，total ${total} `
      }
    }
  },

  mounted () {
    this.handleFetch(this.queryParams, true)
    const timer = window.setInterval(() => this.handleFetch(this.queryParams, false), 2000)
    this.$once('hook:beforeDestroy', () => {
      clearInterval(timer)
    })
  },

  methods: {
    onSelectChange (selectedRowKeys) {
      console.log(selectedRowKeys)
      this.selectedRowKeys = selectedRowKeys
    },

    handleSearch (value) {
      this.paginationInfo = null
      this.handleFetch({
        name: value,
        ...this.queryParams
      }, true)
    },

    handleBuild (record) {
      check().then((resp) => {
        const success = resp.data == true || resp.data == 'true'
        if (success) {
          this.$swal.fire({
            icon: 'success',
            title: 'The current project is building',
            showConfirmButton: false,
            timer: 2000
          }).then((r)=> {
            build({id: record.id})
          })
        } else {
          this.$swal.fire(
            'Failed',
            'Please check "StreamX Console Workspace" is defined and make sure have read and write permissions',
            'error'
          )
        }
      })
    },

    handleAdd () {
      check().then((resp) => {
        const success = resp.data == true || resp.data == 'true'
        if (success) {
          this.$router.push({ 'path': '/flink/project/add' })
        } else {
          this.$swal.fire(
            'Failed',
            'Please check "StreamX Console Workspace" is defined and make sure have read and write permissions',
            'error'
          )
        }
      })
    },

    handleSeeLog (project) {
      this.controller.consoleName = project.name + ' build log'
      this.controller.visible = true
      this.$nextTick(function () {
        this.handleOpenWS(project)
      })
    },
    handleRemove(project) {
        remove({id: project.id}).then((resp) => {
                   console.log(resp)
                  }).catch((error) => {
                    this.$message.error(error.message)
                  })
    },

    handleOpenWS (project) {
      const rows = parseInt(this.controller.modalStyle.height.replace('px', '')) / 16
      const cols = (document.querySelector('.terminal').offsetWidth - 10) / 8
      this.terminal = new Terminal({
        cursorBlink: true,
        rendererType: 'canvas',
        termName: 'xterm',
        useStyle: true,
        screenKeys: true,
        convertEol: true,
        scrollback: 1000,
        tabstopwidth: 4,
        disableStdin: true,
        rows: parseInt(rows), // 行数
        cols: parseInt(cols),
        fontSize: 14,
        cursorStyle: 'underline', // 光标样式
        theme: {
          foreground: '#AAAAAA', // 字体
          background: '#131D32', // 背景色
          lineHeight: 16
        }
      })
      const container = document.getElementById('terminal')
      this.terminal.open(container, true)
      const socket = new SockJS(baseUrl(true).concat('/websocket'))
      this.stompClient = Stomp.over(socket)
      this.stompClient.connect({}, (success) => {
        this.stompClient.subscribe('/resp/build', (msg) => this.terminal.writeln(msg.body))
        this.stompClient.send('/req/build/' + project.id)
      })
    },

    handleClose () {
      this.stompClient.disconnect()
      this.controller.visible = false
      this.terminal.clear()
      this.terminal.clearSelection()
      this.terminal = null
    },

    handleQuery (state) {
      this.queryParams.buildState = state
      this.handleFetch({
        ...this.queryParams
      }, true)
    },

    handleFetch (params, loading) {
      if (loading) {
        this.loading = true
      }
      if (this.paginationInfo) {
        // 如果分页信息不为空，则设置表格当前第几页，每页条数，并设置查询分页参数
        this.$refs.TableInfo.pagination.current = this.paginationInfo.current
        this.$refs.TableInfo.pagination.pageSize = this.paginationInfo.pageSize
        params.pageSize = this.paginationInfo.pageSize
        params.pageNum = this.paginationInfo.current
      } else {
        // 如果分页信息为空，则设置为默认值
        params.pageSize = this.pagination.defaultPageSize
        params.pageNum = this.pagination.defaultCurrent
      }
      list({
        ...params
      }).then((resp) => {
        const pagination = { ...this.pagination }
        pagination.total = parseInt(resp.data.total)
        this.dataSource = resp.data.records
        this.pagination = pagination
        this.loading = false
      })
    }
  }
}
</script>

<style lang="less" scoped>
.ant-avatar-lg {
  width: 48px;
  height: 48px;
  line-height: 48px;
}

.list-content-item {
  color: rgba(0, 0, 0, .45);
  display: inline-block;
  vertical-align: middle;
  font-size: 14px;
  margin-left: 40px;

  span {
    line-height: 20px;
  }

  p {
    margin-top: 4px;
    margin-bottom: 0;
    line-height: 22px;
  }
}

.gutter-box {
  padding: 10px 20px;
  background: #fff;
  color: rgba(0, 0, 0, 0.65);
  font-size: 14px;
  font-variant: tabular-nums;
  line-height: 1.5;
  list-style: none;
  -webkit-font-feature-settings: 'tnum';
  font-feature-settings: 'tnum';
  position: relative;
  border-radius: 2px;
  transition: all 0.3s;
}

.operation {
  width: 80px;
}

.ant-tag {
  border-radius: 0;
  font-weight: 700;
  font-size: 12px;
  text-align: center;
  padding: 0 4px;
  margin-right: 0px;
  cursor: default;
}

.avatar {
  border-radius: 50%;
  background-color: #ebebeb;
  border: 6px solid #ebebeb;
}
</style>
