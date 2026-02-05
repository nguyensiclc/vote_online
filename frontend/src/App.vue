<template>
  <main class="card">
    <header class="card-header">
      <div style="display:flex; align-items:center; justify-content:space-between;">
        <h1 class="card-title">{{ pollTitle || 'Bình chọn tiết mục văn nghệ yêu thích' }}</h1>
      </div>
      <p class="card-subtitle">
        Vui lòng chọn một tiết mục và bấm nút bình chọn.
      </p>
    </header>

    <section v-if="loading" class="loading">
      Đang tải danh sách tiết mục...
    </section>

    <section v-else>
      <div class="candidates" v-if="candidates.length">
        <label
          v-for="c in candidates"
          :key="c.id"
          class="candidate-option"
          :class="{ selected: selectedCandidateId === c.id }"
        >
          <input
            class="radio"
            type="radio"
            name="candidate"
            :value="c.id"
            v-model="selectedCandidateId"
            :disabled="submitting"
          />
          <span class="candidate-label">{{ c.name }}</span>
        </label>
      </div>

      <p v-else class="loading">
        Hiện chưa có tiết mục nào để bình chọn.
      </p>

      <div class="actions">
        <button
          class="btn btn-primary"
          :disabled="!selectedCandidateId || submitting || cooldownSeconds > 0"
          @click="submitVote"
        >
          <span v-if="!submitting">Bình chọn</span>
          <span v-else>Đang gửi...</span>
        </button>

        <p class="helper" v-if="cooldownSeconds <= 0">
          Mỗi thiết bị / mạng chỉ có một phiếu hợp lệ, nhưng bạn có thể thay đổi lựa chọn sau một khoảng thời gian.
        </p>
        <p class="helper" v-else>
          Bạn vừa bình chọn, vui lòng đợi {{ cooldownSeconds }} giây trước khi đổi lựa chọn.
        </p>
      </div>

      <div v-if="successMessage" class="message message-success">
        {{ successMessage }}
      </div>

      <div v-if="errorMessage" class="message message-error">
        {{ errorMessage }}
      </div>
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { apiUrl } from './api.js';

const pollTitle = ref('');
// List of candidates returned from the backend.
const candidates = ref([]);
// Currently selected candidate id.
const selectedCandidateId = ref(null);
// UI state flags.
const loading = ref(false);
const submitting = ref(false);
// Đánh dấu IP này đã từng vote (để hiển thị thông điệp), nhưng vẫn cho phép chỉnh sửa.
const hasVoted = ref(false);
// Thời gian chờ (giây) sau mỗi lần bình chọn thành công để chống spam.
const cooldownSeconds = ref(0);
// Feedback messages.
const successMessage = ref('');
const errorMessage = ref('');
let cooldownInterval = null;

/**
 * Fetch the list of candidates from the backend.
 */
async function loadCandidates() {
  loading.value = true;
  errorMessage.value = '';

  try {
    const path = (window.location.pathname || '/').split('/');
    let pollId = null;
    if (path.length >= 3 && path[1] === 'vote' && path[2]) {
      try {
        const decoded = atob(decodeURIComponent(path[2]));
        const idNum = Number(decoded);
        if (!Number.isNaN(idNum)) {
          pollId = idNum;
        }
      } catch {}
    }
    const url = pollId != null ? `/api/vote/polls/${pollId}/candidates` : '/api/vote/candidates';
    const res = await fetch(apiUrl(url));
    if (!res.ok) {
      throw new Error('Failed to load candidates');
    }
    const data = await res.json();
    candidates.value = data;
  } catch (err) {
    console.error(err);
    errorMessage.value =
      'Không thể tải danh sách tiết mục. Vui lòng tải lại trang hoặc thử lại sau.';
  } finally {
    loading.value = false;
  }
}

/**
 * Load the current vote (nếu có) cho IP hiện tại.
 * Nếu đã vote, tự động chọn lại candidate tương ứng.
 */
async function loadCurrentVote() {
  try {
    const res = await fetch(apiUrl('/api/vote/my-vote'));
    if (res.status === 204) {
      // IP này chưa vote lần nào.
      hasVoted.value = false;
      return;
    }
    if (!res.ok) {
      return;
    }
    const body = await res.json();
    if (body && body.candidateId != null) {
      selectedCandidateId.value = body.candidateId;
      hasVoted.value = true;
      successMessage.value =
        'Bạn đã từng bình chọn. Bạn có thể đổi lựa chọn và bấm Bình chọn lại.';
      const remain = Number(body.cooldownRemainingSeconds ?? 0);
      if (!Number.isNaN(remain) && remain > 0) {
        startCooldown(remain);
      }
    }
  } catch (err) {
    console.error('Failed to load current vote', err);
  }
}

/**
 * Submit the selected vote to the backend.
 * Cho phép ghi đè (edit) vote trước đó của cùng IP.
 */
async function submitVote() {
  if (!selectedCandidateId.value || submitting.value) {
    return;
  }

  submitting.value = true;
  successMessage.value = '';
  errorMessage.value = '';

  try {
    const res = await fetch(apiUrl('/api/vote'), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        candidateId: selectedCandidateId.value
      })
    });

    if (res.ok) {
      const body = await res.json().catch(() => ({}));
      successMessage.value =
        body.message || 'Cảm ơn bạn! Phiếu bình chọn của bạn đã được ghi nhận.';
      hasVoted.value = true;
      const cooldown = Number(body.cooldownSeconds ?? 10);
      startCooldown(Number.isNaN(cooldown) ? 10 : cooldown);
    } else {
      const body = await res.json().catch(() => ({}));
      errorMessage.value =
        body.message || 'Không thể gửi bình chọn. Vui lòng thử lại sau.';
      const remain = Number(body.cooldownRemainingSeconds ?? 0);
      if (!Number.isNaN(remain) && remain > 0) {
        startCooldown(remain);
      }
    }
  } catch (err) {
    console.error(err);
    errorMessage.value =
      'Có lỗi mạng khi gửi bình chọn. Vui lòng thử lại.';
  } finally {
    submitting.value = false;
  }
}

/**
 * Sau mỗi lần bình chọn thành công, kích hoạt thời gian chờ 10s để tránh spam.
 */
function startCooldown(seconds) {
  if (cooldownInterval) {
    clearInterval(cooldownInterval);
    cooldownInterval = null;
  }
  cooldownSeconds.value = Math.max(Number(seconds) || 0, 0);
  const until = Date.now() + cooldownSeconds.value * 1000;
  localStorage.setItem('voteCooldownUntil', String(until));
  cooldownInterval = setInterval(() => {
    const remainMs = until - Date.now();
    const remain = Math.max(Math.ceil(remainMs / 1000), 0);
    cooldownSeconds.value = remain;
    if (remain <= 0) {
      clearInterval(cooldownInterval);
      cooldownInterval = null;
      localStorage.removeItem('voteCooldownUntil');
    }
  }, 250);
}

onMounted(async () => {
  const path = (window.location.pathname || '/').split('/');
  let pollId = null;
  if (path.length >= 3 && path[1] === 'vote' && path[2]) {
    try {
      const decoded = atob(decodeURIComponent(path[2]));
      const idNum = Number(decoded);
      if (!Number.isNaN(idNum)) {
        pollId = idNum;
      }
    } catch {}
  }
  try {
    if (pollId != null) {
      const res = await fetch(apiUrl(`/api/vote/polls/${pollId}`));
      if (res.ok) {
        const data = await res.json();
        pollTitle.value = data?.title || '';
      }
    } else {
      const res = await fetch(apiUrl('/api/vote/polls'));
      if (res.ok) {
        const arr = await res.json();
        pollTitle.value = (arr && arr.length ? arr[0].title : '') || '';
      }
    }
  } catch {}
  const untilRaw = localStorage.getItem('voteCooldownUntil');
  if (untilRaw) {
    const untilNum = Number(untilRaw);
    if (!Number.isNaN(untilNum)) {
      const remainMs = untilNum - Date.now();
      const remain = Math.max(Math.ceil(remainMs / 1000), 0);
      if (remain > 0) {
        startCooldown(remain);
      } else {
        localStorage.removeItem('voteCooldownUntil');
      }
    } else {
      localStorage.removeItem('voteCooldownUntil');
    }
  }
  loadCandidates();
  loadCurrentVote();
});
</script>

