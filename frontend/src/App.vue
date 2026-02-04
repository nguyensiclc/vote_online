<template>
  <main class="card">
    <header class="card-header">
      <h1 class="card-title">Vote for Your Favorite Performance</h1>
      <p class="card-subtitle">
        Please select exactly one performance and submit your vote.
      </p>
    </header>

    <section v-if="loading" class="loading">
      Loading candidates...
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
        No candidates available.
      </p>

      <div class="actions">
        <button
          class="btn btn-primary"
          :disabled="!selectedCandidateId || submitting"
          @click="submitVote"
        >
          <span v-if="!submitting">Vote</span>
          <span v-else>Submitting...</span>
        </button>

        <p class="helper">
          You can have only one active vote from this device / network, but you can change your choice at any time.
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
      'Unable to load candidates. Please refresh the page or try again later.';
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
        'You have already voted. You can change your choice and press Vote again.';
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
        body.message || 'Thank you! Your vote has been recorded.';
      hasVoted.value = true;
    } else {
      const body = await res.json().catch(() => ({}));
      errorMessage.value =
        body.message || 'Unable to submit your vote. Please try again later.';
    }
  } catch (err) {
    console.error(err);
    errorMessage.value =
      'A network error occurred while submitting your vote. Please try again.';
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  loadCandidates();
  loadCurrentVote();
});
</script>

