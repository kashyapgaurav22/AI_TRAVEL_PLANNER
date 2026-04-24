/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#14213d',
        sand: '#fdf0d5',
        flame: '#f77f00',
        ember: '#d62828',
        mint: '#2a9d8f',
      },
      fontFamily: {
        heading: ['Sora', 'sans-serif'],
        body: ['Space Grotesk', 'sans-serif'],
      },
      boxShadow: {
        glow: '0 10px 40px rgba(215, 40, 40, 0.20)',
      },
    },
  },
  plugins: [],
};
