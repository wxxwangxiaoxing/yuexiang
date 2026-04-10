import request from '../utils/request';
import { getBaseUrl } from '../config/app';

function decodeChunk(chunk) {
  if (typeof chunk === 'string') return chunk;
  if (!chunk) return '';
  if (typeof TextDecoder !== 'undefined') {
    if (chunk instanceof Uint8Array) {
      return new TextDecoder('utf-8').decode(chunk, { stream: true });
    }
    if (chunk instanceof ArrayBuffer) {
      return new TextDecoder('utf-8').decode(new Uint8Array(chunk), { stream: true });
    }
    if (chunk?.buffer instanceof ArrayBuffer) {
      return new TextDecoder('utf-8').decode(new Uint8Array(chunk.buffer), { stream: true });
    }
  }
  const buffer = chunk instanceof ArrayBuffer ? chunk : chunk?.buffer;
  return buffer ? String.fromCharCode(...new Uint8Array(buffer)) : '';
}

function emitParsedBlock(block, onEvent) {
  const lines = block.split('\n');
  let eventName = 'message';
  const dataLines = [];

  lines.forEach((line) => {
    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim();
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trim());
    }
  });

  if (!dataLines.length) return;

  try {
    onEvent?.(eventName, JSON.parse(dataLines.join('\n')));
  } catch (error) {
    onEvent?.(eventName, { content: dataLines.join('\n') });
  }
}

function parseSseBuffer(rawText, state, onEvent) {
  state.buffer += rawText.replace(/\r\n/g, '\n');
  const blocks = state.buffer.split('\n\n');
  state.buffer = blocks.pop() || '';
  blocks.forEach((block) => emitParsedBlock(block, onEvent));
}

function flushSseBuffer(state, onEvent) {
  const tail = state.buffer.trim();
  if (!tail) return;
  emitParsedBlock(tail, onEvent);
  state.buffer = '';
}

function createStreamFallback(data, handlers) {
  let aborted = false;
  const done = request({
    url: '/api/ai/chat',
    method: 'POST',
    data
  }).then((res) => {
    if (aborted) return;
    handlers?.onSession?.({ sessionId: res?.sessionId, title: res?.title });
    const messages = res?.messages || [];
    const content = messages[messages.length - 1]?.content || '';
    if (content) {
      handlers?.onDelta?.({ content });
    }
    handlers?.onDone?.(res);
  }).catch((error) => {
    if (!aborted) {
      handlers?.onError?.(error);
    }
    throw error;
  });

  return {
    abort() {
      aborted = true;
    },
    done
  };
}

function dispatchEvent(event, payload, handlers, state) {
  if (event === 'session') handlers?.onSession?.(payload);
  if (event === 'delta') handlers?.onDelta?.(payload);
  if (event === 'done') {
    state.doneReceived = true;
    handlers?.onDone?.(payload);
  }
  if (event === 'error') handlers?.onError?.(payload);
}

function streamWithFetch(data, handlers) {
  const controller = new AbortController();
  const state = { buffer: '', doneReceived: false };
  const token = uni.getStorageSync('token');
  const done = fetch(`${getBaseUrl()}/api/ai/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(data),
    signal: controller.signal
  }).then(async (response) => {
    if (!response.ok || !response.body) {
      throw new Error('流式请求失败');
    }
    const reader = response.body.getReader();
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      parseSseBuffer(decodeChunk(value), state, (event, payload) => {
        dispatchEvent(event, payload, handlers, state);
      });
    }
    flushSseBuffer(state, (event, payload) => {
      dispatchEvent(event, payload, handlers, state);
    });
    if (!state.doneReceived) {
      handlers?.onDone?.({});
    }
  }).catch((error) => {
    if (error?.name !== 'AbortError') {
      handlers?.onError?.(error);
      throw error;
    }
  });

  return {
    abort() {
      controller.abort();
    },
    done
  };
}

function streamWithUniRequest(data, handlers) {
  const state = { buffer: '', doneReceived: false };
  const token = uni.getStorageSync('token');
  let requestTask;
  let fallbackMode = false;

  const done = new Promise((resolve, reject) => {
    requestTask = uni.request({
      url: `${getBaseUrl()}/api/ai/chat/stream`,
      method: 'POST',
      enableChunked: true,
      timeout: 60000,
      data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      success: () => {
        flushSseBuffer(state, (event, payload) => {
          dispatchEvent(event, payload, handlers, state);
        });
        if (!state.doneReceived) {
          handlers?.onDone?.({});
        }
        resolve();
      },
      fail: (error) => {
        if (fallbackMode) return;
        handlers?.onError?.(error);
        reject(error);
      }
    });

    if (requestTask?.onChunkReceived) {
      requestTask.onChunkReceived((chunk) => {
        parseSseBuffer(decodeChunk(chunk?.data), state, (event, payload) => {
          dispatchEvent(event, payload, handlers, state);
        });
      });
      return;
    }

    fallbackMode = true;
    requestTask?.abort?.();
    const fallback = createStreamFallback(data, handlers);
    fallback.done.then(resolve).catch(reject);
  });

  return {
    abort() {
      requestTask?.abort?.();
    },
    done
  };
}

export default {
  chat(data) {
    return request({
      url: '/api/ai/chat',
      method: 'POST',
      data
    });
  },

  chatStream(data, handlers = {}) {
    // #ifdef H5
    if (typeof fetch === 'function') {
      return streamWithFetch(data, handlers);
    }
    // #endif
    return streamWithUniRequest(data, handlers);
  },

  getSessionDetail(sessionId) {
    return request({
      url: `/api/ai/session/${sessionId}`,
      method: 'GET'
    });
  },

  deleteSession(sessionId) {
    return request({
      url: `/api/ai/session/${sessionId}`,
      method: 'DELETE'
    });
  },

  generateTitle(data) {
    return request({
      url: '/api/ai/blog/title',
      method: 'POST',
      data
    });
  },

  polishContent(data) {
    return request({
      url: '/api/ai/blog/polish',
      method: 'POST',
      data
    });
  },

  expandContent(data) {
    return request({
      url: '/api/ai/blog/expand',
      method: 'POST',
      data
    });
  },

  suggestTags(data) {
    return request({
      url: '/api/ai/blog/tags',
      method: 'POST',
      data
    });
  }
};
