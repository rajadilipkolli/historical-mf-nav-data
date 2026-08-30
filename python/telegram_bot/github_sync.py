import os
import json
from github import Github
from github.GithubException import UnknownObjectException

GITHUB_TOKEN = os.getenv("GITHUB_PAT")
GITHUB_REPO = os.getenv("GITHUB_REPO") # e.g. 'username/historical-mf-nav-data'
WATCHLIST_FILE = "watchlist.json"

def get_watchlist():
    """Fetches the watchlist from the GitHub repository."""
    if not GITHUB_TOKEN or not GITHUB_REPO:
        # Fallback to local file if no token
        if os.path.exists(WATCHLIST_FILE):
            with open(WATCHLIST_FILE, "r") as f:
                return json.load(f)
        return []
        
    g = Github(GITHUB_TOKEN)
    try:
        repo = g.get_repo(GITHUB_REPO)
        file_content = repo.get_contents(WATCHLIST_FILE)
        return json.loads(file_content.decoded_content.decode('utf-8'))
    except UnknownObjectException:
        return []
    except Exception as e:
        print(f"Error fetching watchlist from GitHub: {e}")
        return []

def save_watchlist(watchlist):
    """Saves the watchlist to the GitHub repository."""
    if not GITHUB_TOKEN or not GITHUB_REPO:
        # Fallback to local
        with open(WATCHLIST_FILE, "w") as f:
            json.dump(watchlist, f, indent=4)
        return True
        
    g = Github(GITHUB_TOKEN)
    repo = g.get_repo(GITHUB_REPO)
    content = json.dumps(watchlist, indent=4)
    
    try:
        file_info = repo.get_contents(WATCHLIST_FILE)
        repo.update_file(
            WATCHLIST_FILE,
            "Update watchlist via Telegram Bot",
            content,
            file_info.sha
        )
    except UnknownObjectException:
        repo.create_file(
            WATCHLIST_FILE,
            "Create watchlist via Telegram Bot",
            content
        )
    except Exception as e:
        print(f"Error saving watchlist to GitHub: {e}")
        return False
    return True

def add_to_watchlist(scheme_code, scheme_name):
    watchlist = get_watchlist()
    # Check if already exists
    if any(item['scheme_code'] == scheme_code for item in watchlist):
        return False, "Fund is already in your watchlist."
        
    watchlist.append({"scheme_code": scheme_code, "scheme_name": scheme_name})
    success = save_watchlist(watchlist)
    return success, "Fund added successfully."

def remove_from_watchlist(scheme_code):
    watchlist = get_watchlist()
    original_len = len(watchlist)
    watchlist = [item for item in watchlist if str(item['scheme_code']) != str(scheme_code)]
    
    if len(watchlist) == original_len:
        return False, "Fund not found in watchlist."
        
    success = save_watchlist(watchlist)
    return success, "Fund removed successfully."
