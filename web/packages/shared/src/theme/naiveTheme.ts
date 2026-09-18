/** Ant Design 默认主色及悬停/按下态（blue.primary / blue[4] / blue[6]） */
export const antPrimaryColor = '#1677FF'
export const antPrimaryColorHover = '#4096FF'
export const antPrimaryColorPressed = '#0958D9'

/** Naive UI 主题覆盖：对齐 Ant Design 主色 */
export const naiveThemeOverrides = {
  common: {
    primaryColor: antPrimaryColor,
    primaryColorHover: antPrimaryColorHover,
    primaryColorPressed: antPrimaryColorPressed,
    primaryColorSuppl: antPrimaryColor,
  },
}
