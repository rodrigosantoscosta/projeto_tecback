import * as React from 'react'
import * as Popover from '@radix-ui/react-popover'
import { Command } from 'cmdk'
import { Check, ChevronsUpDown } from 'lucide-react'
import { cn } from '../../lib/utils'
import { Button } from './button'

export interface ComboboxOption {
  value: string
  label: string
}

interface ComboboxProps {
  options: ComboboxOption[]
  value: string
  onChange: (value: string) => void
  placeholder?: string
  searchPlaceholder?: string
  emptyMessage?: string
  disabled?: boolean
}

export function Combobox({
  options,
  value,
  onChange,
  placeholder = 'Selecionar...',
  searchPlaceholder = 'Buscar...',
  emptyMessage = 'Nenhum resultado.',
  disabled,
}: ComboboxProps) {
  const [open, setOpen] = React.useState(false)
  const selected = options.find(o => o.value === value)

  return (
    <Popover.Root open={open} onOpenChange={setOpen}>
      <Popover.Trigger asChild>
        <Button
          type="button"
          variant="outline"
          role="combobox"
          aria-expanded={open}
          disabled={disabled}
          className={cn(
            'w-full justify-between font-normal',
            !selected && 'text-muted-foreground'
          )}
        >
          {selected ? selected.label : placeholder}
          <ChevronsUpDown size={14} className="ml-2 shrink-0 text-muted-foreground" />
        </Button>
      </Popover.Trigger>

      <Popover.Portal>
        <Popover.Content
          className="z-50 w-[var(--radix-popover-trigger-width)] rounded-md border border-border bg-popover text-popover-foreground shadow-xl p-0"
          align="start"
          sideOffset={4}
        >
          <Command className="flex flex-col">
            <div className="flex items-center border-b border-border px-3">
              <Command.Input
                placeholder={searchPlaceholder}
                className="flex h-9 w-full bg-transparent py-3 text-sm text-foreground placeholder:text-muted-foreground outline-none"
              />
            </div>
            <Command.List className="max-h-60 overflow-y-auto py-1">
              <Command.Empty className="py-6 text-center text-sm text-muted-foreground">
                {emptyMessage}
              </Command.Empty>
              {options.map(opt => (
                <Command.Item
                  key={opt.value}
                  value={opt.label}
                  onSelect={() => {
                    onChange(opt.value === value ? '' : opt.value)
                    setOpen(false)
                  }}
                  className="flex items-center gap-2 px-3 py-2 text-sm text-foreground cursor-pointer hover:bg-muted data-[selected=true]:bg-muted"
                >
                  <Check
                    size={14}
                    className={cn('shrink-0', value === opt.value ? 'opacity-100' : 'opacity-0')}
                  />
                  {opt.label}
                </Command.Item>
              ))}
            </Command.List>
          </Command>
        </Popover.Content>
      </Popover.Portal>
    </Popover.Root>
  )
}
