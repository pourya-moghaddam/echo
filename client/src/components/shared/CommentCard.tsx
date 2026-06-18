import { useState, useEffect } from "react"
import { Link } from "react-router-dom"
import { MessageSquare, MoreHorizontal, CornerDownRight } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Textarea } from "@/components/ui/textarea"
import { VoteWidget } from "./VoteWidget"
import { useMutation, useQueryClient } from "@tanstack/react-query"
import { postService } from "@/services/post"
import { formatTimeAgo } from "@/lib/utils"

export interface CommentProps {
  id: string | number
  authorUsername: string
  authorAvatar?: string
  createdAt: string
  content: string
  score: number
  replies?: CommentProps[]
  isRoot?: boolean
  userVote?: 'up' | 'down' | null
}

export function CommentCard({ comment }: { comment: CommentProps }) {
  const queryClient = useQueryClient()
  const [showReplies, setShowReplies] = useState(false)
  const [isReplying, setIsReplying] = useState(false)
  const [replyContent, setReplyContent] = useState("")

  const authorName = comment.authorUsername || "unknown"
  const avatarLetter = authorName[0]?.toUpperCase() || "?"
  const timeAgoStr = comment.createdAt ? formatTimeAgo(comment.createdAt) : ""

  const [localScore, setLocalScore] = useState(comment.score)
  const [localVote, setLocalVote] = useState<'up' | 'down' | null>(comment.userVote || null)

  useEffect(() => {
    setLocalScore(comment.score)
    setLocalVote(comment.userVote || null)
  }, [comment.score, comment.userVote])

  const submitReply = useMutation({
    mutationFn: () => postService.replyToComment(comment.id, replyContent),
    onSuccess: () => {
      setReplyContent("")
      setIsReplying(false)
      setShowReplies(true) // Expand replies to show the new comment
      queryClient.invalidateQueries({ queryKey: ['comments'] })
    }
  })

  const handleVoteClick = async (dir: 'up' | 'down') => {
    let newVote: 'up' | 'down' | null = dir
    let scoreChange = 0

    if (localVote === dir) {
      newVote = null
      scoreChange = dir === 'up' ? -1 : 1
    } else if (localVote === 'up' && dir === 'down') {
      scoreChange = -2
    } else if (localVote === 'down' && dir === 'up') {
      scoreChange = 2
    } else {
      scoreChange = dir === 'up' ? 1 : -1
    }

    setLocalVote(newVote)
    setLocalScore(prev => prev + scoreChange)

    try {
      const apiDir = newVote ? newVote.toUpperCase() as 'UP'|'DOWN' : 'NONE'
      await postService.voteComment(comment.id, apiDir)
    } catch (e) {
      setLocalVote(localVote)
      setLocalScore(localScore)
    }
  }

  const hasReplies = comment.replies && comment.replies.length > 0;

  return (
    <div className={`relative flex flex-col ${comment.isRoot ? "pt-4" : "pt-2"} w-full`}>
      {/* Thread Line connecting replies */}
      {showReplies && (
        <div className="absolute left-4 top-10 bottom-0 w-0.5 bg-border/50 hover:bg-border cursor-pointer transition-colors" onClick={() => setShowReplies(false)} title="Collapse replies" />
      )}

      <div className="flex gap-2">
        <Avatar className="h-8 w-8 shrink-0 mt-1">
          <AvatarImage src={comment.authorAvatar} />
          <AvatarFallback>{avatarLetter}</AvatarFallback>
        </Avatar>

        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1">
            <Link to={`/u/${authorName}`} className="text-sm font-semibold hover:underline">
              {authorName}
            </Link>
            <span className="text-xs text-muted-foreground">{timeAgoStr}</span>
          </div>

          <p className="text-sm text-foreground mb-2 break-words">
            {comment.content}
          </p>
          
          <div className="flex items-center gap-2 -ml-2 mb-2">
            <VoteWidget score={localScore} userVote={localVote} onVote={handleVoteClick} direction="horizontal" />
            <Button variant="ghost" size="sm" className="h-8 gap-1.5 text-muted-foreground" onClick={() => setIsReplying(!isReplying)}>
              <MessageSquare className="h-4 w-4" />
              <span className="text-xs font-medium">Reply</span>
            </Button>
            <Button variant="ghost" size="sm" className="h-8 w-8 p-0 text-muted-foreground">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </div>

          {isReplying && (
            <div className="mt-2 mb-4 pl-2 border-l-2 border-border/50">
              <Textarea
                placeholder="What are your thoughts?"
                value={replyContent}
                onChange={(e) => setReplyContent(e.target.value)}
                className="min-h-[2.5rem] max-h-32 resize-y text-sm mb-2"
                rows={1}
              />
              <div className="flex justify-end gap-2">
                <Button variant="ghost" size="sm" onClick={() => setIsReplying(false)}>Cancel</Button>
                <Button size="sm" onClick={() => submitReply.mutate()} disabled={!replyContent.trim() || submitReply.isPending}>
                  {submitReply.isPending ? 'Replying...' : 'Reply'}
                </Button>
              </div>
            </div>
          )}

          {/* Replies logic */}
          {hasReplies && !showReplies && (
            <Button variant="ghost" size="sm" className="h-6 mt-1 mb-1 text-xs text-muted-foreground" onClick={() => setShowReplies(true)}>
              <CornerDownRight className="h-3 w-3 mr-1" />
              Expand replies ({comment.replies!.length})
            </Button>
          )}

          {hasReplies && showReplies && (
            <div className="pl-2 mt-1">
              {comment.replies!.map(reply => (
                <CommentCard key={reply.id} comment={reply} />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
