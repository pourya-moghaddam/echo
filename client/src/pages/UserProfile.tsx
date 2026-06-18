import { useState } from "react"
import { useParams, Link } from "react-router-dom"
import { useQuery } from "@tanstack/react-query"
import { userService } from "@/services/user"
import { postService } from "@/services/post"
import { PostCard } from "@/components/shared/PostCard"
import { CommentCard } from "@/components/shared/CommentCard"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"

export default function UserProfile() {
  const { username } = useParams<{ username: string }>()

  const { data: profile, isLoading: isProfileLoading, error: profileError } = useQuery({
    queryKey: ['user', username],
    queryFn: () => userService.getUserProfile(username!),
    enabled: !!username,
  })

  const { data: postsData, isLoading: isPostsLoading } = useQuery({
    queryKey: ['userPosts', username],
    queryFn: () => postService.getUserPosts(username!),
    enabled: !!username,
  })

  const { data: commentsData, isLoading: isCommentsLoading } = useQuery({
    queryKey: ['userComments', username],
    queryFn: () => postService.getUserComments(username!),
    enabled: !!username,
  })

  const handleVotePost = async (id: string, dir: 'up' | 'down' | 'none') => {
    try {
      await postService.votePost(Number(id), dir.toUpperCase() as any)
    } catch (e) {
      console.error('Vote failed', e)
    }
  }

  if (isProfileLoading) return <div className="p-8 text-center text-muted-foreground">Loading profile...</div>
  if (profileError || !profile) return <div className="p-8 text-center text-destructive">User not found</div>


  const renderPost = (post: any) => (
    <PostCard key={`post-${post.id}`} post={post} onVote={handleVotePost} />
  )

  const renderComment = (comment: any) => (
    <div key={`comment-${comment.id}`} className="bg-card border border-border rounded-lg overflow-hidden shadow-sm hover:shadow-md transition-shadow">
       <div className="p-3 bg-muted/30 border-b text-xs text-muted-foreground flex gap-1">
          <span>Commented on</span>
          <Link to={`/post/${comment.postId}`} className="font-semibold text-foreground hover:underline">{comment.postTitle}</Link>
          <span>in</span>
          <Link to={`/c/${comment.communityName}`} className="font-semibold text-foreground hover:underline">e/{comment.communityName}</Link>
       </div>
       <div className="p-2">
         <CommentCard comment={comment} />
       </div>
    </div>
  )

  return (
    <div className="container mx-auto py-8 px-4 max-w-4xl">
      <div className="flex flex-col md:flex-row items-start md:items-center gap-6 mb-8">
        <Avatar className="h-24 w-24 border-2 border-primary/20">
          <AvatarImage src={profile.avatarUrl} />
          <AvatarFallback className="text-3xl">{profile.username[0].toUpperCase()}</AvatarFallback>
        </Avatar>
        <div className="flex-1">
          <h1 className="text-3xl font-bold text-foreground mb-1">{profile.username}</h1>
        </div>
      </div>

      <Tabs defaultValue="all" className="w-full flex flex-col">
        <TabsList className="mb-6 justify-start">
          <TabsTrigger value="all">All</TabsTrigger>
          <TabsTrigger value="posts">Posts</TabsTrigger>
          <TabsTrigger value="comments">Comments</TabsTrigger>
        </TabsList>

        <TabsContent value="all" className="space-y-8 outline-none mt-0">
          <div>
            <h2 className="text-xl font-semibold mb-4 pb-2 border-b">Posts</h2>
            <div className="space-y-4">
              {isPostsLoading ? (
                <div className="text-center py-8 text-muted-foreground">Loading posts...</div>
              ) : postsData?.content.length === 0 ? (
                <div className="text-center py-12 text-muted-foreground bg-muted/20 rounded-lg border border-dashed">
                  This user hasn't posted anything yet.
                </div>
              ) : (
                postsData?.content.map((post) => renderPost(post))
              )}
            </div>
          </div>
          <div>
            <h2 className="text-xl font-semibold mb-4 pb-2 border-b">Comments</h2>
            <div className="space-y-4">
              {isCommentsLoading ? (
                <div className="text-center py-8 text-muted-foreground">Loading comments...</div>
              ) : commentsData?.content.length === 0 ? (
                <div className="text-center py-12 text-muted-foreground bg-muted/20 rounded-lg border border-dashed">
                  This user hasn't commented on anything yet.
                </div>
              ) : (
                commentsData?.content.map((comment: any) => renderComment(comment))
              )}
            </div>
          </div>
        </TabsContent>

        <TabsContent value="posts" className="space-y-4 outline-none mt-0">
          {isPostsLoading ? (
            <div className="text-center py-8 text-muted-foreground">Loading posts...</div>
          ) : postsData?.content.length === 0 ? (
            <div className="text-center py-12 text-muted-foreground bg-muted/20 rounded-lg border border-dashed">
              This user hasn't posted anything yet.
            </div>
          ) : (
            postsData?.content.map((post) => renderPost(post))
          )}
        </TabsContent>

        <TabsContent value="comments" className="space-y-4 outline-none mt-0">
          {isCommentsLoading ? (
            <div className="text-center py-8 text-muted-foreground">Loading comments...</div>
          ) : commentsData?.content.length === 0 ? (
            <div className="text-center py-12 text-muted-foreground bg-muted/20 rounded-lg border border-dashed">
              This user hasn't commented on anything yet.
            </div>
          ) : (
            commentsData?.content.map((comment: any) => renderComment(comment))
          )}
        </TabsContent>
      </Tabs>
    </div>
  )
}
