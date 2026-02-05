<template>
  <main class="card" v-if="homeStage === 'menu'">
    <header class="card-header">
      <div style="display:flex; align-items:center; justify-content:space-between;">
        <h1 class="card-title">Chọn chức năng</h1>
        <a href="/" class="home-icon-btn" aria-label="Trang chủ">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M3 10.5L12 3l9 7.5v9a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 19.5v-9z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
            <path d="M9 21v-6h6v6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </a>
      </div>
      <p class="card-subtitle">
        Vui lòng chọn đăng nhập quản trị hoặc vào bình chọn.
      </p>
    </header>
    <div class="actions">
      <button class="btn btn-primary" @click="homeStage = 'login'">Đăng nhập Admin</button>
      <button class="btn" @click="showVoteList">Bình chọn</button>
    </div>
  </main>

  <main class="card" v-else-if="homeStage === 'vote-list'">
    <header class="card-header">
      <div style="display:flex; align-items:center; justify-content:space-between;">
        <h1 class="card-title">Danh sách cuộc bình chọn</h1>
        <a href="/" class="home-icon-btn" aria-label="Trang chủ">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M3 10.5L12 3l9 7.5v9a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 19.5v-9z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
            <path d="M9 21v-6h6v6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </a>
      </div>
      <p class="card-subtitle">Chọn một chủ đề để vào bình chọn.</p>
    </header>
    <div class="admin-menu" v-if="polls.length">
      <div
        v-for="p in polls"
        :key="p.id"
        style="display: flex; gap: 0.5rem; align-items: center;"
      >
        <button class="menu-item" style="flex:1" @click="enterVote(p.id)">
          {{ p.title }}
        </button>
        <button class="btn btn-primary" @click="enterVote(p.id)">Bình chọn</button>
      </div>
    </div>
    <p class="helper" v-else>Hiện chưa có chủ đề bình chọn nào.</p>
    <div style="margin-top:0.75rem">
      <button class="tab-button" @click="homeStage = 'menu'">Quay lại</button>
    </div>
  </main>

  <main class="card" v-else>
    <header class="card-header">
      <div style="display:flex; align-items:center; justify-content:space-between;">
        <h1 class="card-title">Đăng nhập hệ thống bình chọn</h1>
        <a href="/" class="home-icon-btn" aria-label="Trang chủ">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M3 10.5L12 3l9 7.5v9a1.5 1.5 0 0 1-1.5 1.5h-15A1.5 1.5 0 0 1 3 19.5v-9z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
            <path d="M9 21v-6h6v6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </a>
      </div>
      <p class="card-subtitle">
        Vui lòng đăng nhập để quản lý cuộc bình chọn và xem kết quả.
      </p>
    </header>
    <section class="admin-login">
      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-row">
          <label for="username">Tài khoản</label>
          <input id="username" v-model="username" type="text" autocomplete="username" placeholder="admin" />
        </div>
        <div class="form-row">
          <label for="password">Mật khẩu</label>
          <input id="password" v-model="password" type="password" autocomplete="current-password" placeholder="Nhập mật khẩu" @keyup.enter="handleLogin" />
        </div>
        <button class="btn btn-primary" type="submit" :disabled="loading || !usernameTrimmed || !passwordTrimmed">
          <span v-if="!loading">Đăng nhập</span>
          <span v-else>Đang xử lý...</span>
        </button>
      </form>
      <div v-if="errorMessage" class="message message-error" style="margin-top: 0.75rem;">
        {{ errorMessage }}
      </div>
      <div style="margin-top:0.75rem">
        <button class="tab-button" @click="homeStage = 'menu'">Quay lại</button>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, ref } from 'vue';
import { apiUrl } from './api.js';

const homeStage = ref('menu');
const polls = ref([]);

const username = ref('');
const password = ref('');
const loading = ref(false);
const errorMessage = ref('');

const usernameTrimmed = computed(() => username.value.trim());
const passwordTrimmed = computed(() => password.value.trim());

function buildAuthToken() {
  const raw = `${usernameTrimmed.value}:${passwordTrimmed.value}`;
  return btoa(raw);
}

async function showVoteList() {
  try {
    const res = await fetch(apiUrl('/api/vote/polls'));
    if (!res.ok) {
      polls.value = [];
    } else {
      polls.value = await res.json();
    }
  } catch {
    polls.value = [];
  }
  homeStage.value = 'vote-list';
}

function enterVote(id) {
  const code = btoa(String(id));
  window.location.href = `/vote/${encodeURIComponent(code)}`;
}

async function handleLogin() {
  if (!usernameTrimmed.value || !passwordTrimmed.value || loading.value) {
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    const token = buildAuthToken();
    // Gọi thử API bảo vệ để kiểm tra credential
    const res = await fetch(apiUrl('/api/admin/results'), {
      headers: {
        Authorization: 'Basic ' + token
      }
    });

    if (!res.ok) {
      if (res.status === 401 || res.status === 403) {
        errorMessage.value = 'Sai tài khoản hoặc mật khẩu.';
      } else {
        errorMessage.value = 'Không thể đăng nhập. Vui lòng thử lại sau.';
      }
      return;
    }

    // Lưu token để AdminApp sử dụng lại
    localStorage.setItem('adminAuth', token);
    window.location.href = '/admin';
  } catch (err) {
    console.error('Login failed', err);
    errorMessage.value = 'Có lỗi khi đăng nhập. Vui lòng thử lại.';
  } finally {
    loading.value = false;
  }
}
</script>

