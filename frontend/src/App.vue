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
            :disabled="hasVoted || submitting"
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
          :disabled="!selectedCandidateId || submitting || hasVoted"
          @click="submitVote"
        >
          <span v-if="!submitting && !hasVoted">Vote</span>
          <span v-else-if="submitting">Submitting...</span>
          <span v-else>Vote submitted</span>
        </button>

        <p class="helper" v-if="!hasVoted">
          You can only vote once from this device / network.
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
 * Submit the selected vote to the backend.
 */
async function submitVote() {
  if (!selectedCandidateId.value || submitting.value || hasVoted.value) {
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
    } else if (res.status === 409) {
      const body = await res.json().catch(() => ({}));
      errorMessage.value =
        body.message ||
        'A vote from this network has already been recorded.';
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
});
</script>

