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
            try:
                with open(WATCHLIST_FILE, "r") as f:
                    watchlist = json.load(f)
                if not isinstance(watchlist, list):
                    raise ValueError("Watchlist must be a list")
                return watchlist
            except (OSError, ValueError) as e:
                print(f"Error fetching local watchlist: {e}")
                return None
        return []
        
    try:
        g = Github(GITHUB_TOKEN)
        repo = g.get_repo(GITHUB_REPO)
    except Exception as e:
        print(f"Error fetching watchlist repository from GitHub: {e}")
        return None

    try:
        file_content = repo.get_contents(WATCHLIST_FILE)
    except UnknownObjectException:
        return []
    except Exception as e:
        print(f"Error fetching watchlist from GitHub: {e}")
        return None

    try:
        watchlist = json.loads(file_content.decoded_content.decode('utf-8'))
        if not isinstance(watchlist, list):
            raise ValueError("Watchlist must be a list")
        return watchlist
    except Exception as e:
        print(f"Error decoding watchlist from GitHub: {e}")
        return None

def save_watchlist(watchlist):
    """Saves the watchlist to the GitHub repository."""
    if not GITHUB_TOKEN or not GITHUB_REPO:
        # Fallback to local
        try:
            with open(WATCHLIST_FILE, "w") as f:
                json.dump(watchlist, f, indent=4)
            return True
        except OSError as e:
            print(f"Error saving local watchlist: {e}")
            return False

    try:
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
    if watchlist is None:
        return False, "Could not load watchlist; no changes were saved."

    # Check if already exists
    if any(item['scheme_code'] == scheme_code for item in watchlist):
        return False, "Fund is already in your watchlist."
        
    watchlist.append({"scheme_code": scheme_code, "scheme_name": scheme_name})
    success = save_watchlist(watchlist)
    if not success:
        return False, "Could not save watchlist."
    return True, "Fund added successfully."

def remove_from_watchlist(scheme_code):
    watchlist = get_watchlist()
    if watchlist is None:
        return False, "Could not load watchlist; no changes were saved."

    original_len = len(watchlist)
    watchlist = [item for item in watchlist if str(item['scheme_code']) != str(scheme_code)]
    
    if len(watchlist) == original_len:
        return False, "Fund not found in watchlist."
        
    success = save_watchlist(watchlist)
    if not success:
        return False, "Could not save watchlist."
    return True, "Fund removed successfully."
