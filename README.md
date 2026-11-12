# AI Conversation Handoff Format

## Purpose
This project uses a standardized Markdown format (`CONVERSATION_SUMMARY.md`) to capture the full context of a conversation with one AI assistant, so that another AI can seamlessly continue the work.

## How It Works

### 1. The Summary File
`CONVERSATION_SUMMARY.md` contains:
- **Hardware/Environment context** - All relevant device specs and versions
- **User request** - Clear statement of what was asked
- **Architecture overview** - Layers, components, and their responsibilities
- **Implementation details** - Code patterns, APIs, permissions, and next steps
- **Files to create** - A checklist of deliverables

### 2. Using With Another AI
When starting a new conversation with another AI assistant:

1. Share the contents of `CONVERSATION_SUMMARY.md`
2. Say: *"Here is a summary of my previous conversation. Please continue from where it left off."*
3. The new AI will pick up the context and continue implementation.

### 3. Updating the Summary
After each significant exchange, update `CONVERSATION_SUMMARY.md` to reflect:
- New decisions made
- Code written
- Issues discovered
- Remaining tasks

## Example Handoff Prompt

*Newly added: Refer to the `ACTIVITY_LOG.md` for recent changes.*
```
I previously worked with an AI to design a car launcher app for my Nippon Android-9Pro Ultra. 
Here is the conversation summary:

[ paste CONVERSATION_SUMMARY.md contents ]

Please continue implementing the app. The next step is to reverse-engineer the CAN IDs.
```

## Benefits
- **No context loss** - Full project state preserved
- **Cross-AI compatible** - Works with any LLM
- **Version control friendly** - Markdown diffs are clean
- **Self-documenting** - Future developers can understand the design