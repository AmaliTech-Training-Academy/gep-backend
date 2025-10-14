# Branch Protection Rules

## Main Branch
- Require pull request reviews (2 approvals)
- Require status checks (CI/CD pipeline)
- Require branches to be up to date
- No direct pushes allowed

## Develop Branch  
- Require pull request reviews (1 approval)
- Require status checks
- Allow force pushes by admins only

## Feature Branches
- No protection rules
- Delete after merge