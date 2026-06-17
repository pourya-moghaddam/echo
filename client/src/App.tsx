import { BrowserRouter, Routes, Route } from "react-router-dom"
import MainLayout from "./components/layout/MainLayout"
import { ThemeProvider } from "./components/theme-provider"

// Simple placeholders for pages
const Home = () => <div className="rounded-lg border bg-card p-6 shadow-sm">Home Feed placeholder</div>
const Popular = () => <div className="rounded-lg border bg-card p-6 shadow-sm">Popular Posts placeholder</div>
const Community = () => <div className="rounded-lg border bg-card p-6 shadow-sm">Community placeholder</div>
const PostDetails = () => <div className="rounded-lg border bg-card p-6 shadow-sm">Post Details placeholder</div>
const Search = () => <div className="rounded-lg border bg-card p-6 shadow-sm">Search Results placeholder</div>

function App() {
  return (
    <ThemeProvider defaultTheme="system" storageKey="echo-theme">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<MainLayout />}>
            <Route index element={<Home />} />
            <Route path="popular" element={<Popular />} />
            <Route path="c/:communityName" element={<Community />} />
            <Route path="c/:communityName/p/:postId" element={<PostDetails />} />
            <Route path="search" element={<Search />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  )
}

export default App
