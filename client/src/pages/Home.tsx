import { useInfiniteQuery } from "@tanstack/react-query"
import { useStore } from "@/store/useStore"
import { PostCard } from "@/components/shared/PostCard"
import { Button } from "@/components/ui/button"
import { postService } from "@/services/post"
import { Link } from "react-router-dom"

export default function Home() {
  const { currentUser } = useStore()

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    status,
  } = useInfiniteQuery({
    queryKey: ['feed'],
    queryFn: ({ pageParam = 0 }) => postService.getFeed(pageParam as number, 10),
    getNextPageParam: (lastPage) => lastPage.last ? undefined : lastPage.number + 1,
    initialPageParam: 0,
    enabled: !!currentUser, // Only fetch feed if logged in
  })

  const handleVote = async (id: string, dir: 'up' | 'down' | 'none') => {
    // Optimistic UI updates should be handled here or via react-query mutation in Phase 8
    // For now we just call the API
    try {
      await postService.votePost(Number(id), dir.toUpperCase() as any)
      // Ideally invalidate queries here, but Phase 8 handles voting properly
    } catch (e) {
      console.error(e)
    }
  }

  if (!currentUser) {
    return (
      <div className="max-w-3xl mx-auto pb-12 w-full text-center mt-20 space-y-4">
        <h2 className="text-2xl font-bold tracking-tight">Welcome to Echo</h2>
        <p className="text-muted-foreground">Please log in to see your personalized feed.</p>
        <Button asChild>
          <Link to="/popular">Browse Popular Posts</Link>
        </Button>
      </div>
    )
  }

  return (
    <div className="max-w-3xl mx-auto pb-12 w-full">
      <h1 className="text-2xl font-bold mb-6">Your Feed</h1>

      {status === 'pending' ? (
        <div className="text-center text-muted-foreground py-12">Loading feed...</div>
      ) : status === 'error' ? (
        <div className="text-center text-destructive py-12">Failed to load feed.</div>
      ) : (
        <div className="flex flex-col gap-4">
          {data.pages.map((page, i) => (
            <div key={i} className="flex flex-col gap-4">
              {page.content.map((post) => (
                <PostCard key={post.id} post={{...post, id: String(post.id)}} onVote={handleVote} />
              ))}
            </div>
          ))}

          {data.pages[0].content.length === 0 && (
            <div className="text-center text-muted-foreground py-12">
              Your feed is empty. Join some communities to see posts here!
              <div className="mt-4">
                <Button variant="outline" asChild>
                  <Link to="/popular">Browse Popular</Link>
                </Button>
              </div>
            </div>
          )}

          {hasNextPage && (
            <div className="flex justify-center mt-6">
              <Button 
                variant="outline" 
                onClick={() => fetchNextPage()} 
                disabled={isFetchingNextPage}
              >
                {isFetchingNextPage ? 'Loading more...' : 'Load More'}
              </Button>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
