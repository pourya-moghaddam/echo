import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatTimeAgo(dateString: string): string {
  const date = new Date(dateString)
  const now = new Date()
  const seconds = Math.floor((now.getTime() - date.getTime()) / 1000)

  if (seconds < 60) return `${Math.max(0, seconds)} sec. ago`
  
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes} min. ago`
  
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} hr. ago`
  
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days} day${days > 1 ? 's' : ''} ago`
  
  const months = Math.floor(days / 30)
  if (months < 12) return `${months} mo. ago`
  
  const years = Math.floor(days / 365)
  return `${years} yr. ago`
}
