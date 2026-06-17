import { useState, useMemo } from "react"
import { useStore } from "@/store/useStore"
import { PostCard } from "@/components/shared/PostCard"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Flame, Sparkles, TrendingUp } from "lucide-react"

export default function Home() {
  const [filter, setFilter] = useState("hot")
  const { posts, upvotePost, downvotePost } = useStore()

  const handleVote = (id: string, dir: 'up' | 'down') => {
    if (dir === 'up') upvotePost(id)
    else downvotePost(id)
  }

  // Simple client-side sorting based on active tab
  const sortedPosts = useMemo(() => {
    const p = [...posts]
    if (filter === 'top') {
      return p.sort((a, b) => b.score - a.score)
    } else if (filter === 'new') {
      // In a real app we'd compare dates, here we'll just reverse for demonstration
      return p.reverse()
    }
    // "hot" keeps default order (usually algorithm based)
    return p
  }, [posts, filter])

  return (
    <div className="max-w-3xl mx-auto pb-12 w-full">
      {/* Feed Filters */}
      <div className="mb-6">
        <Tabs defaultValue="hot" className="w-full" onValueChange={setFilter}>
          <TabsList className="bg-muted/50">
            <TabsTrigger value="hot" className="gap-2">
              <Flame className="h-4 w-4 text-orange-500" />
              Hot
            </TabsTrigger>
            <TabsTrigger value="new" className="gap-2">
              <Sparkles className="h-4 w-4 text-blue-500" />
              New
            </TabsTrigger>
            <TabsTrigger value="top" className="gap-2">
              <TrendingUp className="h-4 w-4 text-green-500" />
              Top
            </TabsTrigger>
          </TabsList>
        </Tabs>
      </div>

      {/* Feed List */}
      <div className="flex flex-col gap-4">
        {sortedPosts.map(post => (
          <PostCard key={post.id} post={post} onVote={handleVote} />
        ))}
        {sortedPosts.length === 0 && (
          <div className="text-center text-muted-foreground py-12">
            No posts found.
          </div>
        )}
      </div>
    </div>
  )
}
