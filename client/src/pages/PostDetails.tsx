import { useParams } from "react-router-dom"
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query"
import { useState } from "react"
import { PostCard } from "@/components/shared/PostCard"
import { CommentCard } from "@/components/shared/CommentCard"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { postService } from "@/services/post"
import { useStore } from "@/store/useStore"

export default function PostDetails() {
  const { postId } = useParams<{ postId: string }>()
  const id = Number(postId)
  const { currentUser } = useStore()
  const queryClient = useQueryClient()
  const [commentContent, setCommentContent] = useState("")

  const { data: post, status: postStatus } = useQuery({
    queryKey: ['post', id],
    queryFn: () => postService.getPost(id),
    enabled: !!id && !isNaN(id),
  })

  const { data: comments, status: commentsStatus } = useQuery({
    queryKey: ['comments', id],
    queryFn: () => postService.getComments(id),
    enabled: !!id && !isNaN(id),
  })

  const handleVote = async (voteId: string, dir: 'up' | 'down') => {
    try {
      await postService.votePost(Number(voteId), dir.toUpperCase() as any)
      queryClient.invalidateQueries({ queryKey: ['post', id] })
    } catch (e) {
      console.error(e)
    }
  }

  const submitComment = useMutation({
    mutationFn: () => postService.createComment(id, commentContent),
    onSuccess: () => {
      setCommentContent("")
      queryClient.invalidateQueries({ queryKey: ['comments', id] })
    }
  })

  if (postStatus === 'pending') {
    return <div className="w-full text-center py-12 text-muted-foreground">Loading post...</div>
  }

  if (postStatus === 'error' || !post) {
    return <div className="w-full text-center py-12 text-destructive">Failed to load post.</div>
  }

  return (
    <div className="flex flex-col h-full relative">
      <div className="pb-12">
        <PostCard post={{ ...post, id: String(post.id) }} onVote={handleVote} isDetail={true} hideCommunity={true} />

        <div className="mt-6 px-4">
          <h3 className="text-lg font-bold mb-6">Comments</h3>
          
          {commentsStatus === 'pending' ? (
            <div className="text-muted-foreground text-sm">Loading comments...</div>
          ) : commentsStatus === 'error' ? (
            <div className="text-destructive text-sm">Failed to load comments.</div>
          ) : comments?.length === 0 ? (
            <div className="text-muted-foreground text-sm">No comments yet. Be the first to share your thoughts!</div>
          ) : (
            <div className="space-y-6">
              {comments?.map((comment: any) => (
                <CommentCard key={comment.id} comment={{ ...comment, isRoot: true }} />
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Sticky Comment Bar */}
      {currentUser && (
        <div className="sticky bottom-0 mt-auto bg-background/95 backdrop-blur-md border-t border-border/50 p-4 z-10 rounded-t-lg shadow-lg">
          <div className="flex items-end gap-3">
            <Textarea
              placeholder="What are your thoughts?"
              value={commentContent}
              onChange={(e) => setCommentContent(e.target.value)}
              className="min-h-[2.5rem] max-h-32 resize-y flex-1"
              rows={1}
            />
            <Button 
              onClick={() => submitComment.mutate()} 
              disabled={!commentContent.trim() || submitComment.isPending}
              className="mb-0.5"
            >
              {submitComment.isPending ? 'Posting...' : 'Comment'}
            </Button>
          </div>
        </div>
      )}
    </div>
  )
}
