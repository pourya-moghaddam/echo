import { BrowserRouter, Routes, Route } from "react-router-dom"
import { ThemeProvider } from "./components/theme-provider"
import MainLayout from "./components/layout/MainLayout"
import { PostCard } from "./components/shared/PostCard"
import type { PostProps } from "./components/shared/PostCard"
import { CommentCard } from "./components/shared/CommentCard"
import type { CommentProps } from "./components/shared/CommentCard"

const DUMMY_POST: PostProps = {
  id: "1",
  community: "reactjs",
  author: "pourya",
  timeAgo: "2 hours ago",
  title: "Just finished Phase 3 of the frontend development for Echo! What do you think?",
  contentSnippet: "We implemented the PostCard, CommentCard, and VoteWidget components. The design is heavily inspired by modern minimalism, using Shadcn UI and Tailwind CSS.",
  score: 1245,
  commentCount: 42,
  tags: ["Showoff", "Discussion"]
}

const DUMMY_COMMENT: CommentProps = {
  id: "c1",
  author: "reviewer",
  timeAgo: "1 hour ago",
  content: "This looks incredibly clean! I love the space grotesk font choice.",
  score: 350,
  isRoot: true,
  replies: [
    {
      id: "c2",
      author: "pourya",
      timeAgo: "45 mins ago",
      content: "Thanks! We're moving on to Phase 4 soon.",
      score: 120,
    }
  ]
}

import { useState } from "react"

function DummyFeed() {
  const [postVote, setPostVote] = useState<'up' | 'down' | null>(null)

  const handleVote = (id: string, dir: 'up' | 'down') => {
    setPostVote(prev => prev === dir ? null : dir)
  }

  const postWithVote = { ...DUMMY_POST, userVote: postVote }

  return (
    <div className="max-w-2xl mx-auto space-y-6 pb-12">
      <h1 className="text-2xl font-bold tracking-tight mb-4 text-foreground">Phase 3 Preview</h1>
      <PostCard post={postWithVote} onVote={handleVote} />
      
      <div className="pt-6 border-t mt-8">
        <h3 className="font-semibold mb-4 text-foreground">Sample Comment Thread</h3>
        <CommentCard comment={DUMMY_COMMENT} />
      </div>
    </div>
  )
}

export default function App() {
  return (
    <ThemeProvider defaultTheme="dark" storageKey="echo-theme">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<MainLayout />}>
            <Route index element={<DummyFeed />} />
            <Route path="c/:community" element={<div>Community Placeholder</div>} />
            <Route path="search" element={<div>Search Results Placeholder</div>} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  )
}
