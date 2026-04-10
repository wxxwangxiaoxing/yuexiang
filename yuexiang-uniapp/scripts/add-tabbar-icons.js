const fs = require('fs');
const path = require('path');

const tabbarDir = path.join(__dirname, '../static/tabbar');
if (!fs.existsSync(tabbarDir)) {
  fs.mkdirSync(tabbarDir, { recursive: true });
}

// Simple geometric SVG strings
const svgs = {
  'home.svg': '<svg viewBox="0 0 24 24" fill="none" stroke="#999" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" xmlns="http://www.w3.org/2000/svg"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>',
  'home-active.svg': '<svg viewBox="0 0 24 24" fill="#FF5A5F" stroke="none" xmlns="http://www.w3.org/2000/svg"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22" fill="none" stroke="#fff" stroke-width="2"></polyline></svg>',
  
  'explore.svg': '<svg viewBox="0 0 24 24" fill="none" stroke="#999" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="10"></circle><polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76"></polygon></svg>',
  'explore-active.svg': '<svg viewBox="0 0 24 24" fill="none" stroke="#FF5A5F" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="10" fill="#FF5A5F" stroke="none"></circle><polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76" fill="#fff" stroke="none"></polygon></svg>',

  'ai.svg': '<svg viewBox="0 0 24 24" fill="none" stroke="#999" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" xmlns="http://www.w3.org/2000/svg"><rect x="3" y="11" width="18" height="10" rx="2"></rect><circle cx="12" cy="5" r="2"></circle><path d="M12 7v4"></path><line x1="8" y1="16" x2="8" y2="16"></line><line x1="16" y1="16" x2="16" y2="16"></line></svg>',
  'ai-active.svg': '<svg viewBox="0 0 24 24" fill="none" stroke="#6C5CE7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" xmlns="http://www.w3.org/2000/svg"><rect x="3" y="11" width="18" height="10" rx="2" fill="#6C5CE7" stroke="none"></rect><circle cx="12" cy="5" r="2" fill="#fff" stroke="none"></circle><path d="M12 7v4" stroke="#6C5CE7"></path><line x1="8" y1="16" x2="8" y2="16" stroke="#fff" stroke-width="4"></line><line x1="16" y1="16" x2="16" y2="16" stroke="#fff" stroke-width="4"></line></svg>',

  'msg.svg': '<svg viewBox="0 0 24 24" fill="none" stroke="#999" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" xmlns="http://www.w3.org/2000/svg"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>',
  'msg-active.svg': '<svg viewBox="0 0 24 24" fill="#FF5A5F" stroke="none" xmlns="http://www.w3.org/2000/svg"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>',

  'my.svg': '<svg viewBox="0 0 24 24" fill="none" stroke="#999" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" xmlns="http://www.w3.org/2000/svg"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>',
  'my-active.svg': '<svg viewBox="0 0 24 24" fill="#FF5A5F" stroke="none" xmlns="http://www.w3.org/2000/svg"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4" fill="#fff"></circle></svg>'
};

for (const [name, content] of Object.entries(svgs)) {
  fs.writeFileSync(path.join(tabbarDir, name), content);
}

const pagesJsonPath = path.join(__dirname, '../pages.json');
try {
  let data = JSON.parse(fs.readFileSync(pagesJsonPath, 'utf-8'));
  const mappings = {
    'pages/home/index': { i: 'static/tabbar/home.svg', a: 'static/tabbar/home-active.svg' },
    'pages/blog/index': { i: 'static/tabbar/explore.svg', a: 'static/tabbar/explore-active.svg' },
    'pages/ai/chat': { i: 'static/tabbar/ai.svg', a: 'static/tabbar/ai-active.svg' },
    'pages/message/index': { i: 'static/tabbar/msg.svg', a: 'static/tabbar/msg-active.svg' },
    'pages/profile/index': { i: 'static/tabbar/my.svg', a: 'static/tabbar/my-active.svg' }
  };
  
  if (data.tabBar && data.tabBar.list) {
    data.tabBar.list.forEach(item => {
      // Only update if not already inserted to avoid duplicate logic errors
      if (mappings[item.pagePath]) {
        item.iconPath = mappings[item.pagePath].i;
        item.selectedIconPath = mappings[item.pagePath].a;
      }
    });
    fs.writeFileSync(pagesJsonPath, JSON.stringify(data, null, 2));
    console.log('Successfully updated pages.json with JSON.parse');
  }
} catch (e) {
  console.log('JSON.parse failed (might contain comments), fallback to regex');
  let txt = fs.readFileSync(pagesJsonPath, 'utf8');
  txt = txt.replace(/("pagePath":\s*"pages\/home\/index",\s*"text":\s*"首页")/g, '$1,\n        "iconPath": "static/tabbar/home.svg",\n        "selectedIconPath": "static/tabbar/home-active.svg"');
  txt = txt.replace(/("pagePath":\s*"pages\/blog\/index",\s*"text":\s*"探店")/g, '$1,\n        "iconPath": "static/tabbar/explore.svg",\n        "selectedIconPath": "static/tabbar/explore-active.svg"');
  txt = txt.replace(/("pagePath":\s*"pages\/ai\/chat",\s*"text":\s*"AI助手")/g, '$1,\n        "iconPath": "static/tabbar/ai.svg",\n        "selectedIconPath": "static/tabbar/ai-active.svg"');
  txt = txt.replace(/("pagePath":\s*"pages\/message\/index",\s*"text":\s*"消息")/g, '$1,\n        "iconPath": "static/tabbar/msg.svg",\n        "selectedIconPath": "static/tabbar/msg-active.svg"');
  txt = txt.replace(/("pagePath":\s*"pages\/profile\/index",\s*"text":\s*"我的")/g, '$1,\n        "iconPath": "static/tabbar/my.svg",\n        "selectedIconPath": "static/tabbar/my-active.svg"');
  fs.writeFileSync(pagesJsonPath, txt);
}
