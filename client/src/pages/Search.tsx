import { useSearchParams, Link } from "react-router-dom"
import { useQuery } from "@tanstack/react-query"
import { searchService } from "@/services/search"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Skeleton } from "@/components/ui/skeleton"

export default function Search() {
  const [searchParams] = useSearchParams()
  const q = searchParams.get('q') || ''

  const { data: communities, isLoading: isLoadingCommunities } = useQuery({
    queryKey: ['search', 'communities', q],
    queryFn: () => searchService.searchCommunities(q),
    enabled: q.length > 0,
  })

  const { data: posts, isLoading: isLoadingPosts } = useQuery({
    queryKey: ['search', 'posts', q],
    queryFn: () => searchService.searchPosts(q),
    enabled: q.length > 0,
  })

  if (!q) {
    return (
      <div className="flex-1 p-6 flex flex-col items-center justify-center text-center">
        <h2 className="text-2xl font-semibold mb-2">Search Echo</h2>
        <p className="text-muted-foreground">Enter a search term in the search bar above.</p>
      </div>
    )
  }

  // isLoading not used directly anymore

  return (
    <div className="flex-1 p-6 max-w-3xl mx-auto w-full">
      <h1 className="text-2xl font-bold mb-6">Search results for "{q}"</h1>

      <Tabs defaultValue="all" className="w-full flex flex-col">
        <TabsList className="mb-6">
          <TabsTrigger value="all">All</TabsTrigger>
          <TabsTrigger value="posts">Posts</TabsTrigger>
          <TabsTrigger value="communities">Communities</TabsTrigger>
        </TabsList>

        <TabsContent value="all" className="space-y-8 mt-0">
          <div>
            <h2 className="text-xl font-semibold mb-4 pb-2 border-b">Communities</h2>
            <CommunityResults communities={communities} isLoading={isLoadingCommunities} limit={3} />
          </div>
          <div>
            <h2 className="text-xl font-semibold mb-4 pb-2 border-b">Posts</h2>
            <PostResults posts={posts} isLoading={isLoadingPosts} limit={10} />
          </div>
        </TabsContent>

        <TabsContent value="posts" className="mt-0">
          <PostResults posts={posts} isLoading={isLoadingPosts} />
        </TabsContent>

        <TabsContent value="communities" className="mt-0">
          <CommunityResults communities={communities} isLoading={isLoadingCommunities} />
        </TabsContent>
      </Tabs>
    </div>
  )
}

function CommunityResults({ communities, isLoading, limit }: { communities?: any[], isLoading: boolean, limit?: number }) {
  if (isLoading) return <LoadingSkeletons count={3} />
  if (!communities || communities.length === 0) return <p className="text-muted-foreground">No communities found.</p>

  const displayList = limit ? communities.slice(0, limit) : communities

  return (
    <div className="space-y-3">
      {displayList.map(c => (
        <Link to={`/c/${c.name}`} key={c.id} className="block p-4 border rounded-lg hover:border-foreground/50 transition-colors bg-card text-card-foreground">
          <div className="flex justify-between items-start">
            <div>
              <h3 className="font-bold text-lg">e/{c.name}</h3>
              <p className="text-sm text-muted-foreground mt-1">{c.description}</p>
            </div>
            <span className="text-xs px-2 py-1 bg-secondary rounded-full">{c.category}</span>
          </div>
        </Link>
      ))}
    </div>
  )
}

function PostResults({ posts, isLoading, limit }: { posts?: any[], isLoading: boolean, limit?: number }) {
  if (isLoading) return <LoadingSkeletons count={5} />
  if (!posts || posts.length === 0) return <p className="text-muted-foreground">No posts found.</p>

  const displayList = limit ? posts.slice(0, limit) : posts

  return (
    <div className="space-y-4">
      {displayList.map(p => (
        <Link to={`/post/${p.id}`} key={p.id} className="block p-4 border rounded-lg hover:border-foreground/50 transition-colors bg-card text-card-foreground">
          <div className="text-xs text-muted-foreground mb-2 flex gap-1">
            <span className="font-semibold text-foreground">e/{p.communityName}</span>
            <span>•</span>
            <span>Posted by u/{p.authorUsername}</span>
          </div>
          <h3 className="font-bold text-lg mb-2">{p.title}</h3>
          <p className="text-sm text-muted-foreground line-clamp-3">{p.content}</p>
        </Link>
      ))}
    </div>
  )
}

function LoadingSkeletons({ count }: { count: number }) {
  return (
    <div className="space-y-4">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="p-4 border rounded-lg space-y-3">
          <Skeleton className="h-4 w-1/4" />
          <Skeleton className="h-6 w-3/4" />
          <Skeleton className="h-4 w-full" />
        </div>
      ))}
    </div>
  )
}
