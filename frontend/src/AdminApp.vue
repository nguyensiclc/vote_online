<template>
  <main class="card admin-container">
    <header class="card-header">
      <div style="display:flex; align-items:center; justify-content:space-between;">
        <h1 class="card-title">Kết quả bình chọn văn nghệ (Admin)</h1>
        <a href="/" class="home-icon-btn" aria-label="Trang chủ">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M3 10.5L12 3l9 7.5v9a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 19.5v-9z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
            <path d="M9 21v-6h6v6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </a>
      </div>
    </header>

    <section v-if="loading" class="loading">
      Đang tải kết quả bình chọn...
    </section>

    <section v-else-if="isAuthenticated">
      <div class="admin-layout">
        <aside class="admin-sidebar">
          <div class="admin-sidebar-header">
            <div class="admin-title">Quản trị</div>
            <button class="tab-button" @click="logout">Đăng xuất</button>
          </div>
          <nav class="admin-menu">
            <button
              class="menu-item"
              :class="{ active: activeTab === 'results' }"
              @click="activeTab = 'results'"
            >
              Kết quả bình chọn
            </button>
            <button
              class="menu-item"
              :class="{ active: activeTab === 'manage' }"
              @click="activeTab = 'manage'"
            >
              Quản lý danh sách bình chọn
            </button>
          </nav>
        </aside>

        <section class="admin-content">
          <section v-if="activeTab === 'results'">
            <div v-if="resultsStage === 'list'">
              <h2 class="card-title" style="font-size: 1.1rem;">Danh sách chủ đề bình chọn</h2>
              <div class="admin-menu">
                <button
                  v-for="p in polls"
                  :key="p.id"
                  class="menu-item"
                  @click="selectedResultsPollId = p.id; resultsStage = 'detail'; loadResults();"
                >
                  {{ p.title }}
                </button>
              </div>
              <p class="helper" style="margin-top: 0.75rem;">Chọn chủ đề để xem bảng kết quả.</p>
            </div>
            <div v-else-if="resultsStage === 'detail'">
              <table v-if="results.length" class="results-table">
                <thead>
                  <tr>
                    <th>STT</th>
                    <th>Tiết mục</th>
                    <th>Tổng số phiếu</th>
                    <th>Tỉ lệ (%)</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(r, index) in results" :key="r.candidateId">
                    <td>{{ index + 1 }}</td>
                    <td>{{ r.candidateName }}</td>
                    <td>{{ r.totalVotes }}</td>
                    <td>{{ formatPercent(r.totalVotes, totalVotes) }}</td>
                  </tr>
                </tbody>
              </table>
              <p v-else class="loading">Chưa có phiếu bình chọn nào.</p>
              <div style="margin-top: 0.75rem;">
                <button class="tab-button" @click="resultsStage = 'list'; selectedResultsPollId = null">Quay lại danh sách chủ đề</button>
              </div>
            </div>
          </section>

          <section v-else-if="activeTab === 'manage'">
            <div v-if="managingStage === 'list'">
              <div style="display:flex; align-items:center; justify-content:space-between;">
                <h2 class="card-title" style="font-size: 1.1rem;">Danh sách chủ đề bình chọn</h2>
                <button class="icon-btn" title="Thêm chủ đề" aria-label="Thêm chủ đề" @click="addPoll">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                    <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </button>
              </div>
              <div class="admin-menu">
                <div
                  v-for="p in polls"
                  :key="p.id"
                  style="display: flex; gap: 0.5rem; align-items: center;"
                >
                  <button
                    class="menu-item"
                    style="flex: 1;"
                    @click="selectedPollId = p.id; managingStage = 'detail'; loadPollConfig(p.id);"
                  >
                    {{ p.title }}
                  </button>
                  <button class="icon-btn" title="Chỉnh sửa" aria-label="Chỉnh sửa" @click="selectedPollId = p.id; managingStage = 'detail'; loadPollConfig(p.id);">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                      <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25z" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M14.06 6.19l3.75 3.75" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                  </button>
                  <button class="icon-btn danger" title="Xóa" aria-label="Xóa" @click="deletePoll(p.id)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                      <path d="M6 7h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                      <path d="M9 7V5h6v2" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M7 7l1 12h8l1-12" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                    </svg>
                  </button>
                  <button class="icon-btn" title="QR code" aria-label="QR code" @click="openQr(p.id)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                      <path d="M4 4h6v6H4V4zM14 4h6v6h-6V4zM4 14h6v6H4v-6zM14 14h2v2h-2v-2zM18 14h2v2h-2v-2zM16 18h2v2h-2v-2zM20 18h2v2h-2v-2z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/>
                    </svg>
                  </button>
                </div>
              </div>
              <p class="helper" style="margin-top: 0.75rem;">
                Chọn một chủ đề để xem chi tiết và chỉnh sửa các option.
              </p>
            </div>

            <div v-else-if="managingStage === 'detail'">
              <h2 class="card-title" style="font-size: 1.1rem;">Chi tiết chủ đề</h2>
              <form class="poll-config-form" @submit.prevent="savePollConfig">
                <div class="form-row">
                  <label for="poll-title">Tiêu đề chủ đề</label>
                  <input
                    id="poll-title"
                    v-model="pollTitle"
                    type="text"
                    placeholder="Nhập tiêu đề chủ đề bình chọn"
                  />
                </div>
                <div class="form-row">
                  <label for="poll-options">
                    Danh sách option (mỗi dòng là một option)
                  </label>
                  <textarea
                    id="poll-options"
                    v-model="pollOptionsText"
                    rows="6"
                    placeholder="Ví dụ:
Tiết mục văn nghệ số 1
Tiết mục văn nghệ số 2
Tiết mục văn nghệ số 3"
                  ></textarea>
                </div>
                <p class="helper">
                  Khi lưu, hệ thống sẽ xóa toàn bộ phiếu hiện tại và tạo mới danh sách option theo nội dung bạn nhập.
                </p>
                <button class="btn btn-primary" type="submit" :disabled="savingConfig">
                  <span v-if="!savingConfig">Lưu cấu hình chủ đề</span>
                  <span v-else>Đang lưu...</span>
                </button>
              </form>


              <div v-if="configMessage" class="message message-success" style="margin-top: 0.75rem;">
                {{ configMessage }}
              </div>
              <div v-if="configError" class="message message-error" style="margin-top: 0.75rem;">
                {{ configError }}
              </div>
              <div style="margin-top: 0.75rem;">
                <button class="tab-button" @click="managingStage = 'list'; selectedPollId = null">Quay lại danh sách chủ đề</button>
              </div>
            </div>
            <div v-else-if="managingStage === 'create'">
              <h2 class="card-title" style="font-size: 1.1rem;">Thêm chủ đề mới</h2>
              <form class="poll-config-form" @submit.prevent="saveNewPoll">
                <div class="form-row">
                  <label for="new-poll-title">Tiêu đề chủ đề</label>
                  <input
                    id="new-poll-title"
                    v-model="pollTitle"
                    type="text"
                    placeholder="Nhập tiêu đề chủ đề bình chọn"
                  />
                </div>
                <div class="form-row">
                  <label for="new-poll-options">
                    Danh sách option (mỗi dòng là một option)
                  </label>
                  <textarea
                    id="new-poll-options"
                    v-model="pollOptionsText"
                    rows="6"
                    placeholder="Ví dụ:
Tiết mục văn nghệ số 1
Tiết mục văn nghệ số 2
Tiết mục văn nghệ số 3"
                  ></textarea>
                </div>
                <p class="helper">
                  Lưu sẽ tạo mới chủ đề và thiết lập danh sách option tương ứng.
                </p>
                <button class="btn btn-primary" type="submit" :disabled="savingConfig">
                  <span v-if="!savingConfig">Tạo chủ đề</span>
                  <span v-else>Đang lưu...</span>
                </button>
              </form>
              <div v-if="configMessage" class="message message-success" style="margin-top: 0.75rem;">
                {{ configMessage }}
              </div>
              <div v-if="configError" class="message message-error" style="margin-top: 0.75rem;">
                {{ configError }}
              </div>
              <div style="margin-top: 0.75rem;">
                <button class="tab-button" @click="managingStage = 'list'; selectedPollId = null">Quay lại danh sách chủ đề</button>
              </div>
            </div>
          </section>
        </section>
      </div>
    </section>
    <div v-if="qrModalVisible" class="qr-modal-backdrop" @click.self="qrModalVisible = false">
      <div class="qr-modal">
        <div class="card-title" style="font-size: 1rem;">QR truy cập bình chọn</div>
        <img :src="qrImageUrl" alt="QR code" />
        <div style="word-break: break-all; font-size: 0.85rem; opacity: 0.8;">
          {{ qrTargetUrl }}
        </div>
        <div class="actions">
          <button class="btn btn-primary" @click="copyQrUrl">Sao chép link</button>
          <button class="tab-button" @click="qrModalVisible = false">Đóng</button>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { apiUrl } from './api.js';

const loading = ref(false);
const results = ref([]);
const errorMessage = ref('');
const totalVotes = ref(0);
const isAuthenticated = ref(false);
const activeTab = ref('results');
const managingStage = ref('list'); // 'list' | 'detail'
const selectedPollId = ref(null);
const resultsStage = ref('list'); // 'list' | 'detail'
const selectedResultsPollId = ref(null);

// Quản lý cấu hình chủ đề bình chọn
const pollTitle = ref('');
const pollOptionsText = ref('');
const savingConfig = ref(false);
const configMessage = ref('');
const configError = ref('');
const qrModalVisible = ref(false);
const qrImageUrl = ref('');
const qrTargetUrl = ref('');

function buildAuthHeader() {
  const token = localStorage.getItem('adminAuth') || '';
  return token ? 'Basic ' + token : '';
}

// Polls
const polls = ref([]);
async function loadPolls() {
  try {
    const res = await fetch(apiUrl('/api/admin/polls'), {
      headers: { Authorization: buildAuthHeader() }
    });
    if (!res.ok) return;
    polls.value = await res.json();
  } catch (err) {
    console.error('Failed to load polls', err);
  }
}

/**
 * Tải kết quả bình chọn từ API admin.
 */
async function loadResults() {
  loading.value = true;
  errorMessage.value = '';

  try {
    const res = await fetch(apiUrl('/api/admin/results'), {
      headers: {
        Authorization: buildAuthHeader()
      }
    });
    if (!res.ok) {
      if (res.status === 401 || res.status === 403) {
        errorMessage.value =
          'Sai tài khoản hoặc mật khẩu admin.';
        isAuthenticated.value = false;
      } else {
        errorMessage.value = 'Không thể tải kết quả bình chọn. Vui lòng thử lại sau.';
      }
      return;
    }

    const data = await res.json();
    results.value = data;
    totalVotes.value = data.reduce((sum, item) => sum + (item.totalVotes ?? 0), 0);
    isAuthenticated.value = true;
  } catch (err) {
    console.error(err);
    errorMessage.value =
      'Có lỗi mạng khi tải kết quả. Vui lòng thử lại.';
  } finally {
    loading.value = false;
  }
}

function logout() {
  localStorage.removeItem('adminAuth');
  window.location.href = '/';
}

function formatPercent(count, total) {
  if (!total || total === 0) {
    return '0%';
  }
  const value = (count / total) * 100;
  return `${value.toFixed(1)}%`;
}

/**
 * Tải cấu hình chủ đề bình chọn (tiêu đề + danh sách option).
 */
async function loadPollConfig(id) {
  try {
    const targetId = id ?? selectedPollId.value;
    if (!targetId) return;
    const res = await fetch(apiUrl(`/api/admin/polls/${targetId}/config`), {
      headers: {
        Authorization: buildAuthHeader()
      }
    });
    if (!res.ok) {
      return;
    }
    const data = await res.json();
    pollTitle.value = data.title ?? '';
    pollOptionsText.value = (data.options ?? []).join('\n');
  } catch (err) {
    console.error('Failed to load poll config', err);
  }
}

async function deletePoll(id) {
  try {
    const res = await fetch(apiUrl(`/api/admin/polls/${id}`), {
      method: 'DELETE',
      headers: { Authorization: buildAuthHeader() }
    });
    if (!res.ok) return;
    await Promise.all([loadPolls(), loadResults()]);
    managingStage.value = 'list';
    selectedPollId.value = null;
  } catch (err) {
    console.error('Failed to delete poll', err);
  }
}

async function addPoll() {
  pollTitle.value = '';
  pollOptionsText.value = '';
  selectedPollId.value = null;
  managingStage.value = 'create';
}

async function saveNewPoll() {
  configMessage.value = '';
  configError.value = '';
  savingConfig.value = true;
  const options = pollOptionsText.value
    .split('\n')
    .map((s) => s.trim())
    .filter((s) => s.length > 0);
  if (!pollTitle.value || options.length === 0) {
    configError.value = 'Vui lòng nhập tiêu đề và ít nhất một option.';
    savingConfig.value = false;
    return;
  }
  try {
    const resCreate = await fetch(apiUrl('/api/admin/polls'), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: buildAuthHeader()
      },
      body: JSON.stringify({ title: pollTitle.value })
    });
    if (!resCreate.ok) {
      configError.value = 'Không thể tạo chủ đề mới. Vui lòng thử lại.';
      savingConfig.value = false;
      return;
    }
    const created = await resCreate.json();
    const resConfig = await fetch(apiUrl(`/api/admin/polls/${created.id}/config`), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: buildAuthHeader()
      },
      body: JSON.stringify({
        title: pollTitle.value,
        options
      })
    });
    if (!resConfig.ok) {
      configError.value = 'Không thể lưu cấu hình chủ đề. Vui lòng thử lại.';
      savingConfig.value = false;
      return;
    }
    configMessage.value = 'Tạo mới chủ đề thành công.';
    await Promise.all([loadPolls(), loadResults()]);
    selectedPollId.value = created.id;
    managingStage.value = 'detail';
    await loadPollConfig(created.id);
  } catch (err) {
    console.error('Failed to create poll', err);
    configError.value = 'Có lỗi khi tạo chủ đề. Vui lòng thử lại.';
  } finally {
    savingConfig.value = false;
  }
}

function openQr(id) {
  const code = btoa(String(id));
  const base = window.location.origin;
  const url = `${base}/vote/${encodeURIComponent(code)}`;
  qrTargetUrl.value = url;
  // use external QR API to avoid bundling libraries
  qrImageUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${encodeURIComponent(url)}`;
  qrModalVisible.value = true;
}

async function copyQrUrl() {
  try {
    await navigator.clipboard.writeText(qrTargetUrl.value);
  } catch {}
}

/**
 * Lưu cấu hình chủ đề bình chọn mới.
 * Sẽ xóa toàn bộ phiếu hiện tại và thay thế danh sách option.
 */
async function savePollConfig() {
  configMessage.value = '';
  configError.value = '';
  savingConfig.value = true;

  try {
    const options = pollOptionsText.value
      .split('\n')
      .map((s) => s.trim())
      .filter((s) => s.length > 0);

    if (!pollTitle.value || options.length === 0) {
      configError.value = 'Vui lòng nhập tiêu đề và ít nhất một option.';
      savingConfig.value = false;
      return;
    }

    if (!selectedPollId.value) {
      configError.value = 'Chưa chọn chủ đề để chỉnh sửa.';
      savingConfig.value = false;
      return;
    }

    const res = await fetch(apiUrl(`/api/admin/polls/${selectedPollId.value}/config`), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: buildAuthHeader()
      },
      body: JSON.stringify({
        title: pollTitle.value,
        options
      })
    });

    if (!res.ok) {
      configError.value = 'Không thể lưu cấu hình chủ đề. Vui lòng thử lại.';
      return;
    }

    configMessage.value =
      'Lưu cấu hình chủ đề thành công. Danh sách option và phiếu đã được làm mới.';

    // Sau khi thay đổi options, reload lại kết quả (số phiếu sẽ về 0)
    await loadResults();
  } catch (err) {
    console.error('Failed to save poll config', err);
    configError.value =
      'Có lỗi khi lưu cấu hình chủ đề. Vui lòng thử lại.';
  } finally {
    savingConfig.value = false;
  }
}

onMounted(() => {
  // Kiểm tra token, nếu không có thì quay lại trang login
  const token = localStorage.getItem('adminAuth');
  if (!token) {
    window.location.href = '/';
    return;
  }

  // Nếu có token, thử load kết quả và cấu hình
  loadResults().then(() => {
    if (isAuthenticated.value) {
      loadPolls();
    } else {
      // Token không hợp lệ -> logout
      logout();
    }
  });
});
</script>


