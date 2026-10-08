# gitoutofhere

init() - inits new git repo under git/ with objects/, HEAD, and index inside
hash() - takes file path then hashes it with SHA-1 and returns the hash string in hex
createBlob() - creates a blob in objects/ with the contents of the file and the hash as the file name
addToIndex() - records files as an entry to the git/index file with the hash and relative path
createTree() - creates a tree for the given directory from the working list and returns its SHA-1 hash
createWorkingList() - creates a working list by adding blobs in front of index entries
createTrees() - creates trees for each folder and its contents until there is only one tree left
findDeepest() - finds and returns the deepest folder in the working list
