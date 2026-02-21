import os
import tempfile
import zipfile

def write_csv_as_zip(csv_bytes, zip_path, inner_name):
    os.makedirs(os.path.dirname(zip_path), exist_ok=True)
    with zipfile.ZipFile(zip_path, 'w', compression=zipfile.ZIP_DEFLATED) as zf:
        zf.writestr(inner_name, csv_bytes)

def extract_csv_from_zip(zip_path, member_name=None, extract_dir=None):
    """Extract a CSV from zip_path. If member_name is None, extract the first .csv found.
    Returns the path to the extracted file.
    """
    if extract_dir is None:
        extract_dir = tempfile.mkdtemp(prefix='navdata_')
    with zipfile.ZipFile(zip_path, 'r') as zf:
        name = member_name
        if name is None:
            for n in zf.namelist():
                if n.endswith('.csv'):
                    name = n
                    break
        if name is None:
            raise ValueError('No CSV member found in zip')
        out_path = os.path.join(extract_dir, os.path.basename(name))
        with open(out_path, 'wb') as f:
            f.write(zf.read(name))
    return out_path
