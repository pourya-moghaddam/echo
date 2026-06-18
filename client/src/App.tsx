import { BrowserRouter, Routes, Route } from "react-router-dom"
import { QueryClient, QueryClientProvider } from "@tanstack/react-query"
import { ThemeProvider } from "./components/theme-provider"
import { TooltipProvider } from "./components/ui/tooltip"
import MainLayout from "./components/layout/MainLayout"
import Home from "./pages/Home"
import Popular from "./pages/Popular"
import Community from "./pages/Community"
import CreatePost from "./pages/CreatePost"

import PostDetails from "./pages/PostDetails"

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      staleTime: 1000 * 60 * 5, // 5 minutes
    },
  },
})

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider defaultTheme="dark" storageKey="echo-theme">
        <TooltipProvider>
          <BrowserRouter>
            <Routes>
              <Route path="/" element={<MainLayout />}>
                <Route index element={<Home />} />
                <Route path="popular" element={<Popular />} />
                <Route path="c/:community" element={<Community />} />
                <Route path="submit" element={<CreatePost />} />
                <Route path="post/:postId" element={<PostDetails />} />
                <Route path="search" element={<div>Search Results Placeholder</div>} />
              </Route>
            </Routes>
          </BrowserRouter>
        </TooltipProvider>
      </ThemeProvider>
    </QueryClientProvider>
  )
}
