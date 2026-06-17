import { useInfiniteQuery } from "@tanstack/react-query"
import { useParams } from "react-router-dom"
import { PostCard } from "@/components/shared/PostCard"
import { Button } from "@/components/ui/button"
import { postService } from "@/services/post"

export default function Community() {
  const { community } = useParams<{ community: string }>()

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    status,
  } = useInfiniteQuery({
    queryKey: ['community-posts', community],
    queryFn: ({ pageParam = 0 }) => postService.getCommunityPosts(community!, pageParam as number, 10),
    getNextPageParam: (lastPage) => lastPage.last ? undefined : lastPage.number + 1,
    initialPageParam: 0,
    enabled: !!community,
  })

  const handleVote = async (id: string, dir: 'up' | 'down') => {
    try {
      await postService.votePost(Number(id), dir.toUpperCase() as any)
    } catch (e) {
      console.error(e)
    }
  }

  return (
    <div className="max-w-3xl mx-auto pb-12 w-full">
      <h1 className="text-2xl font-bold mb-6">c/{community} Posts</h1>

      {status === 'pending' ? (
        <div className="text-center text-muted-foreground py-12">Loading posts...</div>
      ) : status === 'error' ? (
        <div className="text-center text-destructive py-12">Failed to load posts.</div>
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
              There are no posts in this community yet. Be the first to post!
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
