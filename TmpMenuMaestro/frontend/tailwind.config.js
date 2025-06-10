const { addDynamicIconSelectors } = require('@iconify/tailwind');

/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: "#20686C",
          dark: "#005236",
          light: "#6bc4a6",
          50: '#38B6BC',
          100: '#34A7AD',
          200: '#2F989D',
          300: '#2A888D',
          400: '#26797E',
          500: '#20686C', // Base color
          600: '#1C5B5E',
          700: '#174C4F',
          800: '#123D3F',
          900: '#0D2E30',
        },
        secondary: {
          DEFAULT: "#bc4f07",
          dark: "#bc4f07",
          light: "#fe982a",
        },
      },
    },
  },
  plugins: [
    addDynamicIconSelectors(),
  ],
}
