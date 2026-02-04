<template>
  <main class="card">
    <header class="card-header">
      <h1 class="card-title">Bình chọn tiết mục văn nghệ yêu thích</h1>
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

/**
 * Fetch the list of candidates from the backend.
 */
async function loadCandidates() {
  loading.value = true;
  errorMessage.value = '';

  try {
    const res = await fetch('/api/vote/candidates');
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
    const res = await fetch('/api/vote/my-vote');
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
    const res = await fetch('/api/vote', {
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
      startCooldown();
    } else {
      const body = await res.json().catch(() => ({}));
      errorMessage.value =
        body.message || 'Không thể gửi bình chọn. Vui lòng thử lại sau.';
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
function startCooldown() {
  cooldownSeconds.value = 10;
  const interval = setInterval(() => {
    if (cooldownSeconds.value <= 1) {
      cooldownSeconds.value = 0;
      clearInterval(interval);
    } else {
      cooldownSeconds.value -= 1;
    }
  }, 1000);
}

onMounted(() => {
  loadCandidates();
  loadCurrentVote();
});
</script>

