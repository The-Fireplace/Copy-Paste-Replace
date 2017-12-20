#include <iostream>
#include <windows.h>
#include <vector>
#include <sstream>
#include <dirent.h>
#include <fstream>
#include <sys/stat.h>

using namespace std;

vector<string> split(string str, char delimiter) {
	vector<string> internal;
	stringstream ss(str);
	string tok;

	while(getline(ss, tok, delimiter)) {
		internal.push_back(tok);
	}

	return internal;
}

string currentDir() {
	char buffer[MAX_PATH];
	GetModuleFileName(NULL, buffer, MAX_PATH);
	string::size_type pos = string(buffer).find_last_of("\\/");
	return string(buffer).substr(0, pos);
}

int getdir (string dir, vector<string> &files)
{
    DIR *dp;
    struct dirent *dirp;
    if((dp  = opendir(dir.c_str())) == NULL) {
        cout << "Error(" << errno << ") opening " << dir << endl;
        return errno;
    }

    while ((dirp = readdir(dp)) != NULL) {
        files.push_back(string(dirp->d_name));
    }
    closedir(dp);
    return 0;
}

bool replaceAll(std::string& str, const std::string& from, const std::string& to) {
    if(from.empty())
        return false;
    size_t start_pos = 0;
    bool repl = false;
    while((start_pos = str.find(from, start_pos)) != string::npos) {
        str.replace(start_pos, from.length(), to);
        start_pos += to.length();
        repl = true;
    }
    return repl;
}

bool exists(const char *name){
	fstream infile(name);
	return infile.good();
}

int main() {
	SetConsoleTitle("Copy, Paste, Replace. C++ Edition.");
	string rename;
	string renameto;
	cout << "Enter the string to replace: ";
	cin >> rename;
	cout << "Enter the strings to replace it with(separated by colons): ";
	cin >> renameto;

	vector<string> renameTo = split(renameto, ':');
	vector<string> files;
	getdir(currentDir(), files);
	for(string file : files){
		fstream thefile;
		thefile.open(file, fstream::in);
		struct stat s;
		//Ensure that the file is a regular file, not a directory.
		if ( stat(file.c_str(),&s) == 0 )
		    if ( !(s.st_mode & S_IFREG) )
		        continue;//Skip this one if it isn't a regular file
		cout << "Found regular file: "+file << endl;

		//Perform the replacement
		for(string outname:renameTo){
			fstream theoutfile;
			string outfilename = file;
			replaceAll(outfilename, rename, outname);
			//If the file exists, append _alt to the filename
			while(exists(outfilename.c_str())){
				int index = outfilename.find_last_of('.');
				outfilename = outfilename.insert(index, "_alt");
			}
			//the contents of the currently opened file
			string filecont;
			//storage for the current file line
			string fileLine;
			//Add file contents to the filecont variable
			while(getline(thefile, fileLine))
				filecont.append(fileLine+"\n");
			//Reset the getline to 0 for the next file
			thefile.clear();
			thefile.seekg(0, ios::beg);
			//Perform the replacement, and output a file if anything was replaced.
			bool fileChanged = replaceAll(filecont, rename, outname);
			cout << "Was file changed: " << fileChanged << endl;
			if(fileChanged){
				cout << "Output file name: "+outfilename << endl;
				theoutfile.open(outfilename, fstream::out);
				theoutfile << filecont;
				theoutfile.close();
			}
		}
		thefile.close();
	}

	system("pause");
	return 0;
}
