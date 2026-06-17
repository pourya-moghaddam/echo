import { useState } from "react"
import { Link } from "react-router-dom"
import { MessageSquare, MoreHorizontal, CornerDownRight } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { VoteWidget } from "./VoteWidget"

export interface CommentProps {
  id: string
  author: string
  authorAvatar?: string
  timeAgo: string
  content: string
  score: number
  replies?: CommentProps[]
  isRoot?: boolean
}

export function CommentCard({ comment }: { comment: CommentProps }) {
  const [isCollapsed, setIsCollapsed] = useState(false)

  return (
    <div className={`relative flex flex-col ${comment.isRoot ? "pt-4" : "pt-2"} w-full`}>
      {/* Thread Line connecting replies */}
      {!isCollapsed && !comment.isRoot && (
        <div className="absolute left-4 top-10 bottom-0 w-0.5 bg-border/50 hover:bg-border cursor-pointer transition-colors" onClick={() => setIsCollapsed(true)} />
      )}

      <div className="flex gap-2">
        <Avatar className="h-8 w-8 shrink-0 mt-1">
          <AvatarImage src={comment.authorAvatar} />
          <AvatarFallback>{comment.author[0].toUpperCase()}</AvatarFallback>
        </Avatar>

        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1">
            <Link to={`/u/${comment.author}`} className="text-sm font-semibold hover:underline">
              {comment.author}
            </Link>
            <span className="text-xs text-muted-foreground">{comment.timeAgo}</span>
          </div>

          {!isCollapsed && (
            <>
              <p className="text-sm text-foreground mb-2 break-words">
                {comment.content}
              </p>
              
              <div className="flex items-center gap-2 -ml-2 mb-2">
                <VoteWidget score={comment.score} direction="horizontal" />
                <Button variant="ghost" size="sm" className="h-8 gap-1.5 text-muted-foreground">
                  <MessageSquare className="h-4 w-4" />
                  <span className="text-xs font-medium">Reply</span>
                </Button>
                <Button variant="ghost" size="sm" className="h-8 w-8 p-0 text-muted-foreground">
                  <MoreHorizontal className="h-4 w-4" />
                </Button>
              </div>

              {/* Nested Replies */}
              {comment.replies && comment.replies.length > 0 && (
                <div className="pl-2">
                  {comment.replies.map(reply => (
                    <CommentCard key={reply.id} comment={reply} />
                  ))}
                </div>
              )}
            </>
          )}

          {isCollapsed && (
            <Button variant="ghost" size="sm" className="h-6 mt-1 text-xs" onClick={() => setIsCollapsed(false)}>
              <CornerDownRight className="h-3 w-3 mr-1" />
              Expand thread
            </Button>
          )}
        </div>
      </div>
    </div>
  )
}
