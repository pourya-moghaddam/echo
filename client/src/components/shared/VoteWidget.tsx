import { ArrowBigUp, ArrowBigDown } from "lucide-react"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"

interface VoteWidgetProps {
  score: number
  userVote?: 'up' | 'down' | null
  onVote?: (direction: 'up' | 'down') => void
  direction?: 'vertical' | 'horizontal'
}

export function VoteWidget({ score, userVote, onVote, direction = 'vertical' }: VoteWidgetProps) {
  const isVertical = direction === 'vertical'

  return (
    <div className={cn("flex items-center gap-1", isVertical ? "flex-col" : "flex-row")}>
      <Button
        variant="ghost"
        size="icon"
        className={cn(
          "h-8 w-8 rounded-full transition-all duration-300 active:duration-75 hover:bg-orange-500/10 hover:text-orange-500 active:-translate-y-1",
          userVote === 'up' && "text-orange-500 bg-orange-500/10"
        )}
        onClick={(e) => {
          e.preventDefault()
          onVote?.('up')
        }}
      >
        <ArrowBigUp className={cn("h-5 w-5 transition-colors", userVote === 'up' && "fill-current")} />
      </Button>
      <span className={cn(
        "text-sm font-bold transition-colors duration-200",
        userVote === 'up' && "text-orange-500",
        userVote === 'down' && "text-blue-500"
      )}>
        {score >= 1000 ? `${(score / 1000).toFixed(1)}k` : score}
      </span>
      <Button
        variant="ghost"
        size="icon"
        className={cn(
          "h-8 w-8 rounded-full transition-all duration-300 active:duration-75 hover:bg-blue-500/10 hover:text-blue-500 active:translate-y-1",
          userVote === 'down' && "text-blue-500 bg-blue-500/10"
        )}
        onClick={(e) => {
          e.preventDefault()
          onVote?.('down')
        }}
      >
        <ArrowBigDown className={cn("h-5 w-5 transition-colors", userVote === 'down' && "fill-current")} />
      </Button>
    </div>
  )
}
