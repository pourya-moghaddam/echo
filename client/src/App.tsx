import { BrowserRouter, Routes, Route } from "react-router-dom"
import { ThemeProvider } from "./components/theme-provider"
import MainLayout from "./components/layout/MainLayout"
import Home from "./pages/Home"

export default function App() {
  return (
    <ThemeProvider defaultTheme="dark" storageKey="echo-theme">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<MainLayout />}>
            <Route index element={<Home />} />
            <Route path="c/:community" element={<div>Community Placeholder</div>} />
            <Route path="search" element={<div>Search Results Placeholder</div>} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  )
}
