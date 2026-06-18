import { useInfiniteQuery } from "@tanstack/react-query"
import { PostCard } from "@/components/shared/PostCard"
import { Button } from "@/components/ui/button"
import { postService } from "@/services/post"

export default function Popular() {
  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    status,
  } = useInfiniteQuery({
    queryKey: ['popular-posts'],
    queryFn: ({ pageParam = 0 }) => postService.getPopular(pageParam as number, 10),
    getNextPageParam: (lastPage) => lastPage.last ? undefined : lastPage.number + 1,
    initialPageParam: 0,
  })

  const handleVote = async (id: string, dir: 'up' | 'down' | 'none') => {
    try {
      await postService.votePost(Number(id), dir.toUpperCase() as any)
    } catch (e) {
      console.error(e)
    }
  }

  return (
    <div className="max-w-3xl mx-auto pb-12 w-full">
      <h1 className="text-2xl font-bold mb-6 flex items-center gap-2">
        Popular Posts
      </h1>

      {status === 'pending' ? (
        <div className="text-center text-muted-foreground py-12">Loading popular posts...</div>
      ) : status === 'error' ? (
        <div className="text-center text-destructive py-12">Failed to load popular posts.</div>
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
              No popular posts yet.
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
