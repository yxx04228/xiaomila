// components/icons/VolumeIcons.ts - Windows 11 精确风格
import { h } from 'vue'

// 静音图标
export const VolumeMute = {
  name: 'VolumeMute',
  setup() {
    return () =>
      h(
        'svg',
        {
          class: 'icon',
          viewBox: '0 0 24 24',
          width: '1em',
          height: '1em',
          fill: 'currentColor',
          xmlns: 'http://www.w3.org/2000/svg',
        },
        [
          // 喇叭主体
          h('path', {
            d: 'M12 4L7 9H4v6h3l5 5V4z',
          }),
          // 静音叉号
          h('path', {
            d: 'M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3z',
          }),
        ],
      )
  },
}

// 低音量图标 - Windows 11 风格
export const VolumeLow = {
  name: 'VolumeLow',
  setup() {
    return () =>
      h(
        'svg',
        {
          class: 'icon',
          viewBox: '0 0 24 24',
          width: '1em',
          height: '1em',
          fill: 'currentColor',
          xmlns: 'http://www.w3.org/2000/svg',
        },
        [
          // 喇叭主体
          h('path', {
            d: 'M12 4L7 9H4v6h3l5 5V4z',
          }),
          // 小声波
          h('path', {
            d: 'M15.5 12c0-1.1-.45-2.1-1.17-2.83l-1.41 1.41C13.39 10.89 13.5 11.43 13.5 12s-.11 1.11-.58 1.42l1.41 1.41C15.05 14.1 15.5 13.1 15.5 12z',
          }),
        ],
      )
  },
}

// 中音量图标 - Windows 11 风格
export const VolumeMedium = {
  name: 'VolumeMedium',
  setup() {
    return () =>
      h(
        'svg',
        {
          class: 'icon',
          viewBox: '0 0 24 24',
          width: '1em',
          height: '1em',
          fill: 'currentColor',
          xmlns: 'http://www.w3.org/2000/svg',
        },
        [
          // 喇叭主体
          h('path', {
            d: 'M12 4L7 9H4v6h3l5 5V4z',
          }),
          // 中声波
          h('path', {
            d: 'M16.5 12c0-1.93-.78-3.68-2.05-4.95l-1.41 1.41C13.89 9.11 14.5 10.49 14.5 12s-.61 2.89-1.46 3.54l1.41 1.41C15.72 15.68 16.5 13.93 16.5 12z',
          }),
        ],
      )
  },
}

// 高音量图标 - Windows 11 风格
export const VolumeHigh = {
  name: 'VolumeHigh',
  setup() {
    return () =>
      h(
        'svg',
        {
          class: 'icon',
          viewBox: '0 0 24 24',
          width: '1em',
          height: '1em',
          fill: 'currentColor',
          xmlns: 'http://www.w3.org/2000/svg',
        },
        [
          // 喇叭主体
          h('path', {
            d: 'M12 4L7 9H4v6h3l5 5V4z',
          }),
          // 中声波
          h('path', {
            d: 'M16.5 12c0-1.93-.78-3.68-2.05-4.95l-1.41 1.41C13.89 9.11 14.5 10.49 14.5 12s-.61 2.89-1.46 3.54l1.41 1.41C15.72 15.68 16.5 13.93 16.5 12z',
          }),
          // 大声波
          h('path', {
            d: 'M19.5 12c0-2.76-1.12-5.26-2.93-7.07l-1.41 1.41C16.39 7.61 17.5 9.66 17.5 12s-1.11 4.39-2.34 5.66l1.41 1.41C18.38 17.26 19.5 14.76 19.5 12z',
          }),
        ],
      )
  },
}
