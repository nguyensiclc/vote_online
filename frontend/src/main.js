import { createApp } from 'vue';
import './style.css';

// Tải lười các app khác nhau tùy theo URL hiện tại
async function bootstrap() {
  const path = window.location.pathname || '/';

  if (path.startsWith('/admin')) {
    const { default: AdminApp } = await import('./AdminApp.vue');
    createApp(AdminApp).mount('#app');
  } else if (path.startsWith('/vote')) {
    const { default: VoteApp } = await import('./App.vue');
    createApp(VoteApp).mount('#app');
  } else {
    // Mặc định: trang login / chọn chức năng
    const { default: LoginApp } = await import('./LoginApp.vue');
    createApp(LoginApp).mount('#app');
  }
}

bootstrap();

