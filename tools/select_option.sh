#!/usr/bin/env bash

# Interactive arrow-key menu. Returns selected index via $?.
# Usage: select_option "opt1" "opt2" "opt3"; choice=$?
# All UI output goes to /dev/tty so the menu works inside $() command substitutions.
function select_option {
    local ESC key idx selected option_count
    ESC=$(printf "\033")

    option_count=$#
    selected=0

    # Reserve lines for the menu once, then redraw in place.
    for _ in "$@"; do
        printf "\n" >/dev/tty
    done

    trap 'printf "%b[?25h\n" "'"$ESC"'" >/dev/tty; stty echo; exit' INT
    printf "%b[?25l" "$ESC" >/dev/tty

    while true; do
        # Move up by number of options and redraw all lines.
        printf "%b[%dA" "$ESC" "$option_count" >/dev/tty

        idx=0
        for option in "$@"; do
            if [ "$idx" -eq "$selected" ]; then
                # Clear line, then print highlighted option
                printf "%b[2K  %b[7m %s %b[27m\n" "$ESC" "$ESC" "$option" "$ESC" >/dev/tty
            else
                printf "%b[2K   %s\n" "$ESC" "$option" >/dev/tty
            fi
            idx=$((idx + 1))
        done

        # Read up/down/enter from terminal
        IFS= read -rsn3 key </dev/tty 2>/dev/null || key=""

        if [[ "$key" == "$ESC[A" ]]; then
            selected=$((selected - 1))
            if [ "$selected" -lt 0 ]; then
                selected=$((option_count - 1))
            fi
        elif [[ "$key" == "$ESC[B" ]]; then
            selected=$((selected + 1))
            if [ "$selected" -ge "$option_count" ]; then
                selected=0
            fi
        else
            break
        fi
    done

    printf "%b[?25h\n" "$ESC" >/dev/tty
    return "$selected"
}

title="$1"
shift

if [ -z "$title" ]; then
    echo "Missing menu title" >&2
    exit 64
fi

if [ "$#" -eq 0 ]; then
    echo "Missing menu options" >&2
    exit 64
fi

printf "%s\n" "$title" >/dev/tty
select_option "$@"
exit $?
