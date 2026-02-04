import { createApp } from 'vue';
import App from './App.vue';
import './style.css';

// Tải lười AdminApp để chỉ dùng khi cần
async function bootstrap() {
  const path = window.location.pathname || '/';

  if (path.startsWith('/admin')) {
    const { default: AdminApp } = await import('./AdminApp.vue');
    createApp(AdminApp).mount('#app');
  } else {
    createApp(App).mount('#app');
  }
}

bootstrap();

