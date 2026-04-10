import { createSSRApp } from 'vue';
import App from './App.vue';
import store from './store';
import api from './api';

export function createApp() {
  const app = createSSRApp(App);
  app.use(store);
  app.config.globalProperties.$api = api;
  return {
    app,
  };
}
