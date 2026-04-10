import blogApi from './blog';
import userApi from './user';
import shopApi from './shop';
import aiApi from './ai';
import voucherApi from './voucher';
import messageApi from './message';
import authApi from './auth';

export default {
  auth: authApi,
  blog: blogApi,
  user: userApi,
  shop: shopApi,
  ai: aiApi,
  voucher: voucherApi,
  message: messageApi
};
