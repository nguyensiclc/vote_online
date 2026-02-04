<template>
  <main class="card">
    <header class="card-header">
      <h1 class="card-title">Kết quả bình chọn văn nghệ (Admin)</h1>
    </header>

    <section v-if="!isAuthenticated" class="admin-login">
      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-row">
          <label for="password">Mật khẩu admin</label>
          <input
            id="password"
            v-model="password"
            type="password"
            autocomplete="current-password"
            placeholder="Nhập mật khẩu admin"
          />
        </div>
        <button class="btn btn-primary" type="submit" :disabled="loading || !password">
          <span v-if="!loading">Đăng nhập &amp; xem kết quả</span>
          <span v-else>Đang xử lý...</span>
        </button>
      </form>

      <div v-if="errorMessage" class="message message-error" style="margin-top: 0.75rem;">
        {{ errorMessage }}
      </div>
    </section>

    <section v-if="loading" class="loading">
      Đang tải kết quả bình chọn...
    </section>

    <section v-else-if="isAuthenticated">
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

      <p v-else class="loading">
        Chưa có phiếu bình chọn nào.
      </p>
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue';

const loading = ref(false);
const results = ref([]);
const errorMessage = ref('');
const totalVotes = ref(0);
const password = ref('');
const isAuthenticated = ref(false);

function buildAuthHeader() {
  // Username cố định là "admin", chỉ yêu cầu người dùng nhập mật khẩu.
  const raw = `admin:${password.value}`;
  // btoa yêu cầu chuỗi ASCII; với user/pass đơn giản (admin/admin123) là đủ
  return 'Basic ' + btoa(raw);
}

/**
 * Tải kết quả bình chọn từ API admin.
 */
async function loadResults() {
  loading.value = true;
  errorMessage.value = '';

  try {
    const res = await fetch('/api/admin/results', {
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

async function handleLogin() {
  await loadResults();
}

function formatPercent(count, total) {
  if (!total || total === 0) {
    return '0%';
  }
  const value = (count / total) * 100;
  return `${value.toFixed(1)}%`;
}

onMounted(() => {
  // Không tự load kết quả, chờ người dùng nhập tài khoản/mật khẩu
});
</script>


