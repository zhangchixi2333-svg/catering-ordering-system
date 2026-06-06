<template>
  <div class="chat-page">
    <section class="chat-shell">
      <aside class="contact-panel">
        <div class="panel-title">
          <h2>实时对话</h2>
          <span :class="['status-dot', wsConnected ? 'online' : 'offline']"></span>
        </div>

        <div class="current-user">
          <strong>{{ currentName }}</strong>
          <span>{{ roleLabel }} · {{ wsConnected ? '当前在线' : '连接断开' }}</span>
        </div>

        <template v-if="isCustomer">
          <button
            v-for="contact in visibleCustomerContacts"
            :key="contact.key"
            :class="['contact-item', activeContact.key === contact.key ? 'active' : '']"
            @click="selectContact(contact)"
          >
            <span>{{ contact.name }}</span>
            <small>{{ contact.desc }}</small>
            <em>{{ contactOnlineText(contact) }}</em>
          </button>
        </template>

        <template v-else>
          <label class="field-label" for="peerId">用户 ID</label>
          <div class="reply-box">
            <input
              id="peerId"
              v-model.number="manualPeerId"
              type="number"
              min="1"
              placeholder="输入要回复的用户ID"
              @keyup.enter="openManualConversation"
            />
            <button @click="openManualConversation">打开</button>
          </div>

          <button
            v-for="contact in visibleStaffContacts"
            :key="contact.key"
            :class="['contact-item', activeContact.key === contact.key ? 'active' : '']"
            @click="selectContact(contact)"
          >
            <span>{{ contact.name }}</span>
            <small>{{ contact.desc }}</small>
            <em>{{ contactOnlineText(contact) }}</em>
          </button>
        </template>

        <div class="recent-list" v-if="recentMessages.length">
          <h3>最近消息</h3>
          <button
            v-for="item in recentMessages"
            :key="item.messageId"
            class="recent-item"
            @click="openRecent(item)"
          >
            <span>{{ recentTitle(item) }}</span>
            <small>{{ item.content }}</small>
          </button>
        </div>
      </aside>

      <main class="conversation">
        <header class="conversation-header">
          <div>
            <h1>{{ activeContact.name }}</h1>
            <p>{{ activeContact.desc }} · {{ contactOnlineText(activeContact) }}</p>
          </div>
          <button class="refresh-button" :disabled="loading" @click="reloadMessages">
            {{ loading ? '刷新中...' : '刷新' }}
          </button>
        </header>

        <div ref="messageListRef" class="message-list">
          <div v-if="loading && messages.length === 0" class="empty-state">正在加载消息...</div>
          <div v-else-if="messages.length === 0" class="empty-state">暂无对话记录</div>
          <div
            v-for="message in messages"
            :key="message.messageId"
            :class="['message-row', isMine(message) ? 'mine' : 'theirs']"
          >
            <div class="message-bubble">
              <div class="message-meta">
                <span>{{ isMine(message) ? currentName : message.senderName || '对方' }}</span>
                <time>{{ formatTime(message.sendTime) }}</time>
              </div>
              <p>{{ message.content }}</p>
              <small v-if="isMine(message)">{{ deliveryText(message) }}</small>
            </div>
          </div>
        </div>

        <footer class="composer">
          <textarea
            v-model.trim="draft"
            rows="3"
            placeholder="输入消息，Ctrl + Enter 发送"
            @keydown.ctrl.enter.prevent="sendMessage"
          ></textarea>
          <button :disabled="sending || !draft || !activeContact.recipientIds.length" @click="sendMessage">
            {{ sending ? '发送中...' : '发送' }}
          </button>
        </footer>
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { chatApi, createWebSocket } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const draft = ref('')
const loading = ref(false)
const sending = ref(false)
const messages = ref([])
const recentMessages = ref([])
const onlineStatus = ref({})
const messageListRef = ref(null)
const wsConnected = ref(false)
const ws = ref(null)
const manualPeerId = ref(null)

const roleCode = computed(() => {
  const role = userStore.currentRole
  return typeof role === 'object' ? role?.roleCode : role
})

const currentUserId = computed(() => Number(userStore.user?.id || userStore.user?.userId || 0))
const currentName = computed(() => userStore.user?.nickname || userStore.user?.username || `用户${currentUserId.value}`)
const roleLabel = computed(() => {
  const names = { USER: '普通用户', STAFF: '店员', MANAGER: '店长', ADMIN: '管理员' }
  return names[roleCode.value] || '在线用户'
})
const isCustomer = computed(() => roleCode.value === 'USER')

const customerContacts = [
  { key: 'shop-service', name: '店铺服务组', desc: '同时联系店员和店长', recipientIds: [2, 3], peerId: 2 },
  { key: 'staff', name: '店员', desc: '联系前台或服务员', recipientIds: [2], peerId: 2 },
  { key: 'manager', name: '店长', desc: '联系店长处理问题', recipientIds: [3], peerId: 3 }
]

const staffContacts = [
  { key: 'customer-1', name: '用户 1', desc: '默认测试用户', recipientIds: [1], peerId: 1 },
  { key: 'staff', name: '店员', desc: '联系店员协同处理', recipientIds: [2], peerId: 2 },
  { key: 'manager', name: '店长', desc: '联系店长协同处理', recipientIds: [3], peerId: 3 }
]

const activeContact = ref(customerContacts[0])

const withoutCurrentUser = (contacts) => contacts
  .map(contact => ({
    ...contact,
    recipientIds: contact.recipientIds.filter(id => id !== currentUserId.value)
  }))
  .filter(contact => contact.recipientIds.length > 0)

const visibleCustomerContacts = computed(() => withoutCurrentUser(customerContacts))
const visibleStaffContacts = computed(() => withoutCurrentUser(staffContacts))

const normalizeMessage = (payload) => ({
  messageId: payload.messageId || `${Date.now()}-${Math.random()}`,
  senderId: Number(payload.senderId),
  senderName: payload.senderName,
  senderRole: payload.senderRole,
  recipientIds: (payload.recipientIds || []).map(Number),
  shopId: payload.shopId,
  content: payload.content || '',
  sendTime: payload.sendTime || new Date().toISOString(),
  delivered: Boolean(payload.delivered),
  recipientOnlineStatus: payload.recipientOnlineStatus || {}
})

const uniqueMessages = (items) => {
  const map = new Map()
  items.forEach(item => map.set(item.messageId, item))
  return [...map.values()].sort((a, b) => new Date(a.sendTime) - new Date(b.sendTime))
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const isMine = (message) => Number(message.senderId) === currentUserId.value

const isActiveConversationMessage = (message) => {
  const senderId = Number(message.senderId)
  const recipients = (message.recipientIds || []).map(Number)
  if (senderId === currentUserId.value) {
    return activeContact.value.recipientIds.some(id => recipients.includes(id))
  }
  return activeContact.value.recipientIds.includes(senderId)
}

const appendMessage = async (message) => {
  messages.value = uniqueMessages([...messages.value, message])
  await scrollToBottom()
}

const selectContact = async (contact) => {
  activeContact.value = contact
  await reloadMessages()
}

const openManualConversation = async () => {
  const peerId = Number(manualPeerId.value)
  if (!peerId) {
    ElMessage.warning('请输入用户ID')
    return
  }
  await selectContact({
    key: `user-${peerId}`,
    name: `用户 ${peerId}`,
    desc: '按用户ID建立对话',
    recipientIds: [peerId],
    peerId
  })
}

const openRecent = async (item) => {
  const peerId = Number(item.senderId) === currentUserId.value
    ? Number(item.recipientIds?.[0])
    : Number(item.senderId)
  if (!peerId) return
  await selectContact({
    key: `recent-${peerId}`,
    name: Number(item.senderId) === currentUserId.value ? `用户 ${peerId}` : item.senderName || `用户 ${peerId}`,
    desc: '最近消息',
    recipientIds: [peerId],
    peerId
  })
}

const loadConversation = async () => {
  if (!activeContact.value?.peerId) return
  const res = await chatApi.getConversation(activeContact.value.peerId, 200, activeContact.value.recipientIds)
  messages.value = uniqueMessages((res.data || []).map(normalizeMessage))
  await scrollToBottom()
}

const loadRecent = async () => {
  const res = await chatApi.getRecent(50)
  recentMessages.value = uniqueMessages((res.data || []).map(normalizeMessage)).reverse()
}

const loadOnlineStatus = async () => {
  const ids = new Set()
  activeContact.value.recipientIds.forEach(id => ids.add(id))
  customerContacts.concat(staffContacts).forEach(contact => contact.recipientIds.forEach(id => ids.add(id)))
  recentMessages.value.forEach(message => {
    ids.add(message.senderId)
    message.recipientIds.forEach(id => ids.add(id))
  })
  ids.delete(currentUserId.value)
  if (!ids.size) return
  const res = await chatApi.getOnlineStatus([...ids])
  onlineStatus.value = {
    ...onlineStatus.value,
    ...(res.data || {})
  }
}

const reloadMessages = async () => {
  loading.value = true
  try {
    await loadConversation()
    await loadRecent()
    await loadOnlineStatus()
  } catch (error) {
    console.error('刷新消息失败:', error)
  } finally {
    loading.value = false
  }
}

const sendMessage = async () => {
  if (!draft.value || sending.value) return
  sending.value = true
  try {
    const res = await chatApi.send({
      senderName: currentName.value,
      senderRole: roleCode.value,
      shopId: userStore.currentShopId || 1,
      recipientIds: activeContact.value.recipientIds,
      content: draft.value
    })
    draft.value = ''
    await appendMessage(normalizeMessage(res.data))
    await loadRecent()
    await loadOnlineStatus()
  } catch (error) {
    console.error('发送消息失败:', error)
  } finally {
    sending.value = false
  }
}

const handleWsMessage = async (event) => {
  if (event.type === 'USER_STATUS') {
    const userId = event.data?.userId
    if (userId) {
      onlineStatus.value = {
        ...onlineStatus.value,
        [userId]: Boolean(event.data.online)
      }
    }
    return
  }
  if (!['CHAT_MESSAGE', 'CHAT_MESSAGE_SENT'].includes(event.type)) return
  const message = normalizeMessage(event.data || {})
  if (isActiveConversationMessage(message)) {
    await appendMessage(message)
  } else {
    ElMessage.info(`${message.senderName || '用户'} 发来新消息`)
  }
  await loadRecent()
  await loadOnlineStatus()
}

const contactOnlineText = (contact) => {
  const ids = contact.recipientIds.filter(id => id !== currentUserId.value)
  if (!ids.length) return '当前账号'
  const onlineCount = ids.filter(id => onlineStatus.value[id]).length
  if (onlineCount === ids.length) return '全部在线'
  if (onlineCount > 0) return `${onlineCount}/${ids.length} 在线`
  return '离线'
}

const deliveryText = (message) => {
  const recipients = (message.recipientIds || []).filter(id => id !== currentUserId.value)
  if (!recipients.length) return ''
  const status = message.recipientOnlineStatus || {}
  const deliveredCount = recipients.filter(id => status[id] === true || status[String(id)] === true).length
  if (deliveredCount === recipients.length) return '已送达'
  if (deliveredCount > 0) return `${deliveredCount}/${recipients.length} 已送达，其余离线保存`
  return '对方离线，已保存'
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const recentTitle = (item) => {
  if (Number(item.senderId) === currentUserId.value) {
    return `发给 ${item.recipientIds?.join(', ')}`
  }
  return item.senderName || `用户 ${item.senderId}`
}

onMounted(async () => {
  activeContact.value = isCustomer.value ? visibleCustomerContacts.value[0] : visibleStaffContacts.value[0]
  await reloadMessages()

  if (currentUserId.value) {
    ws.value = createWebSocket(currentUserId.value, handleWsMessage)
    ws.value.onopen = async () => {
      wsConnected.value = true
      await reloadMessages()
    }
    ws.value.onclose = () => {
      wsConnected.value = false
    }
  }
})

onBeforeUnmount(() => {
  if (ws.value) {
    ws.value.close()
  }
})
</script>

<style scoped>
.chat-page {
  min-height: calc(100vh - 120px);
}

.chat-shell {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  min-height: calc(100vh - 120px);
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.contact-panel {
  border-right: 1px solid #e5e7eb;
  background: #fafafa;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-title,
.conversation-header,
.message-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-title h2,
.conversation-header h1 {
  margin: 0;
  color: #111827;
}

.panel-title h2 {
  font-size: 20px;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #9ca3af;
}

.status-dot.online {
  background: #10b981;
}

.current-user,
.contact-item,
.recent-item {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
}

.current-user {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #374151;
}

.current-user span,
.contact-item small,
.recent-item small,
.conversation-header p {
  color: #6b7280;
}

.contact-item,
.recent-item {
  width: 100%;
  padding: 12px;
  text-align: left;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.contact-item em {
  color: #2563eb;
  font-style: normal;
  font-size: 12px;
}

.contact-item:hover,
.contact-item.active {
  border-color: #3b82f6;
  background: #eff6ff;
}

.field-label {
  font-size: 13px;
  color: #4b5563;
  font-weight: 600;
}

.reply-box {
  display: flex;
  gap: 8px;
}

.reply-box input {
  min-width: 0;
  flex: 1;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 9px 10px;
}

.reply-box button,
.refresh-button,
.composer button {
  border: 0;
  border-radius: 6px;
  background: #2563eb;
  color: #fff;
  cursor: pointer;
}

.reply-box button,
.refresh-button {
  padding: 8px 12px;
}

.refresh-button:disabled,
.composer button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.recent-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recent-list h3 {
  margin: 0;
  font-size: 14px;
  color: #374151;
}

.recent-item small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation {
  min-width: 0;
  display: grid;
  grid-template-rows: auto 1fr auto;
}

.conversation-header {
  padding: 18px 22px;
  border-bottom: 1px solid #e5e7eb;
}

.conversation-header h1 {
  font-size: 22px;
}

.conversation-header p {
  margin: 5px 0 0;
}

.message-list {
  overflow-y: auto;
  padding: 22px;
  background: #f8fafc;
}

.empty-state {
  height: 100%;
  display: grid;
  place-items: center;
  color: #6b7280;
}

.message-row {
  display: flex;
  margin-bottom: 14px;
}

.message-row.mine {
  justify-content: flex-end;
}

.message-bubble {
  max-width: min(620px, 78%);
  padding: 12px 14px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e5e7eb;
  color: #111827;
}

.message-row.mine .message-bubble {
  background: #2563eb;
  border-color: #2563eb;
  color: #fff;
}

.message-meta {
  gap: 18px;
  font-size: 12px;
  opacity: 0.78;
}

.message-bubble p {
  margin: 8px 0 0;
  white-space: pre-wrap;
  line-height: 1.6;
}

.message-bubble small {
  display: block;
  margin-top: 6px;
  opacity: 0.72;
}

.composer {
  border-top: 1px solid #e5e7eb;
  padding: 16px;
  display: grid;
  grid-template-columns: 1fr 96px;
  gap: 12px;
}

.composer textarea {
  resize: none;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 12px;
  font: inherit;
  line-height: 1.5;
}

.composer button {
  font-size: 15px;
}

@media (max-width: 900px) {
  .chat-shell {
    grid-template-columns: 1fr;
  }

  .contact-panel {
    border-right: 0;
    border-bottom: 1px solid #e5e7eb;
  }

  .composer {
    grid-template-columns: 1fr;
  }
}
</style>
