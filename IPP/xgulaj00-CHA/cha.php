<?php
	/*
	*	Author: Jan Gula(xgulaj00)
	*	Project: IPP Proj1 : C header analysis @ FIT VUT Brno
	*/
	
	#Error constants
	define("NO_ERR", 0);
	define("INVALID_PARAMS", 1);
	define("INVALID_INPUT", 2); #Doesn't exist or can't open file
	define("CANT_OPEN_OUTPUT", 3);
	define("INVALID_INPUT_FORMAT", 4);
	define("TASK_SPECIFIC_ERR", 18);
	define("OTHER_ERR", 111);

	#Default values
	$dirName = "";
	$path = array();
	$output 	= STDOUT;
	$prettyXml  = false;
	$noInline   = false;
	$maxPar 	= false;
	$noDups 	= false;
	$removeWS 	= false;

	/*
	*	Prints help if --help argument is called. This argument can be used alone only.
	*/
	function userHelp(){
		echo "C header analysis[CHA] - Help\n\n";
		echo "--help\nPrints out help.\n\n";
		echo "--input=fileordir\nDetermines input file or directory.\n\n";
		echo "--output=filename\nDetermines output file.\n\n";
		echo "--pretty-xml=k\nFormats the final XML file.\n\n";
		echo "--no-inline\nProgram ignores inline functions.\n\n";
		echo "--max-par=n\nDefines the maximal number of parameters.\n\n";
		echo "--no-duplicates\nIgnores duplicate functions.\n\n";
		echo "--remove-whitespace\nRemoves additional whitespaces from attributes rettype and type.\n";
	}

	/*
	*	Checks if the given parameter isn't already defined
	*/
	function checkRedefinition($params) {
		foreach (array_keys($params) as $arrString) {
			if(count($params[$arrString]) > 1) {
				fprintf(STDERR, "Redefinition!\n");
				exit(INVALID_PARAMS);
			}
		}
		return 0;	
	}

	/*
	*	Checks if necessary arguments have a value
	*/
	function checkParValue($parVal) {
		if($parVal === false) {
			fprintf(STDERR, "Parameter with an undefined value!\n");
			exit(INVALID_PARAMS);
		}
	}

	/*
	*	Finds all .h files in a directory
	*/
	function findFiles($fName) {
		$path = array();
		$realPath = realpath($fName);
		$rdi = new RecursiveDirectoryIterator($realPath);
		$rii = new RecursiveIteratorIterator($rdi);
		foreach ($rii as $foundFile) {
			$isHeader = pathinfo($foundFile, PATHINFO_EXTENSION);
			if($isHeader == 'h')
			array_push($path, $foundFile);		
		}
		return $path;
	}

	/*
	*	Copies the content of a file into a variable and then filters useless information like comments macros or strings
	*/
	function getContent($fName) {
		$functions = array();
		#get whole file content in one variable
		$content = file_get_contents($fName);
		#matches any multiple line comments
		$multipleLinesPatt = "/\/\*.*?\*\//s";
		#matches one line comments
		$oneLinePatt = "/\/\/.*?\n/";
		#matches all macros
		$macrosPatt = "/#.*?\n/";
		#matches any string
		$stringsPatt = "/\".*?\"/";

		# pattern explanation : 1 a-Z or _ character, followed by 0 to n alphanum or space chars, 1 to n graphical chars, 1 to n spaces, 1 to n graphical chars, 0 to n spaces, 1 ( char, 0 to n graphical chars or spaces, one ) char, 0 to n spaces, and one ; or { char every quantifier expands as needed and works as few times as possible (lazy)
		$funPatt = "/[a-zA-Z_][[:alpha:]\s]*?[[:graph:]]+?[\s]+?[[:graph:]]+?[\s]*?\([[:graph:]\s]*?\)[\s]*?[;{]/";

		#delete all /* comments by replacing them with empty strings
		$content = preg_replace($multipleLinesPatt, "", $content);  
 		#delete all // comments
 		$content = preg_replace($oneLinePatt, "", $content); 
 		#delete all macros
 		$content = preg_replace($macrosPatt, "", $content);  
		#delete all strings
		$content = preg_replace($stringsPatt, "", $content);
		#matches any function definition
		$pregMatch=preg_match_all($funPatt, $content, $functions);
		return $functions;
	}

	$longopts = array(
	"help::",
	"input:",
	"output:",
	"pretty-xml::",
	"no-inline::",
	"max-par:",
	"no-duplicates::",
	"remove-whitespace::"
	);

	$options = getopt(NULL, $longopts);
	if ($options === false) {
		fprintf(STDERR, "Invalid parameters!\n");
		exit(INVALID_PARAMS);
	}

	if ((count($argv) - 1) > (count($options))) {
		fprintf(STDERR, "Invalid parameter!\n");
		exit(INVALID_PARAMS);
	}

	$params = $options;
	checkRedefinition($params);
	#parsing arguments
		foreach (array_keys($params) as $arrElem) {

			switch($arrElem) {
				case "help" :
					if (count($params) !== 1) {
						fprintf(STDERR, "Can't combine --help with other parameters!\n");
						exit(INVALID_PARAMS);
					} else {
					userHelp();
					exit(NO_ERR);
					}
				break;
				case "input" :
					checkParValue($arrElem);
					if(!(is_dir($params[$arrElem]))) {
						$path = $params[$arrElem];
						} else {
						# it is a directory so we have to search it through
						$fileName = $params[$arrElem];
						$dirName = $fileName;
						$path = findFiles($fileName);
					}
						#everything is in path[], path[0], path[1] etc.
					if(strlen($path[0]) === 0) {
						fprintf(STDERR, "Invalid input path!\n");
						exit(INVALID_INPUT);
					}
				break;
				case "output" :
					checkParValue($arrElem);
					$output = $params[$arrElem];
				break;
				case "pretty-xml" :
					checkParValue($arrElem);
					$offset = $params[$arrElem];
					$prettyXml = true;
					if(!(is_numeric($offset))) {
						$offset = 4; #Default value
					}
				break;
				case "no-inline" :
					$noInline = true;
				break;
				case "max-par" :
					checkParValue($arrElem);
					$maxPar = $params[$arrElem];
					$maxParBool = true;
				break;
				case "no-duplicates" :
					$noDups = true;
				break;
				case "remove-whitespace" :
					$removeWS = true;
				break;
				default: fprintf(STDERR, "Invalid parameter!\n");
						 exit(INVALID_PARAMS);
			}
		}

	$files = array();
	$files = $path;
	if(!($files)) {
		$files = findFiles('./');
		$dirName = './';
	}

	#Create our output XML file
	$xmlDoc = new DOMDocument('1.0', 'UTF-8');
	$root = $xmlDoc->createElement('functions');
	$root->setAttribute('dir',$dirName);
	$root = $xmlDoc->appendChild($root);

	#two variables for remove-duplicates 
	$dups = array();
	$dupsCount = 0;

	#now on this if statement the code breaks into two parts one solves the case when we have just one file and the other part solves the case when we have multiple files and have to iterate through each. 
	if(!(is_array($files))) {
		$oneFile = $files;
		if(!($handle = @fopen($oneFile, "r")))
			exit(INVALID_INPUT);
		$fncs = getContent($oneFile);
	
		for($i = 0;$i < count($fncs[0]); $i++) {
			#pattern explanation : 1 a-Z or _ char, 0 to n alphanum or spaces, 1 to n graphical chars, 0 to n spaces, 1 to n alphanum or _ chars, 0 to n spaces, and one ( char. Every quantifier expands as needed and works as few times as possible (lazy). Separates the input regexp to two groups first group is the return type of a function and the second group is the name of the function
			$noParamsPattern = "/([a-zA-Z_][[:alpha:]\s]*?[[:graph:]]+?)[\s]*?([[:alnum:]_]+?)[\s]*?\(/";
			preg_match($noParamsPattern, $fncs[0][$i], $retTypeFunName);
			#pattern explanation : 1 ( char, 0 to n characters except ^ and ) but as many times as possible, giving back as needed (greedy). Separates the function parameters from it's return type and identificator
			preg_match("/\([^\)]*/", $fncs[0][$i],$res);
			#delete the ( at the start of the string
			$allParams = substr($res[0], 1);
			$paramsSep = explode(",", $allParams);
			#is ... defined as a parameter ? yes : no
			$varArgsPatt = "/\.\.\./u";			
			$isVar = preg_match($varArgsPatt, $fncs[0][$i]);
			if($isVar === 1) {
				$varArgs = "yes";
			} else {
				$varArgs = "no";
			}

			#removes inline functions
			#pattern explanation: 1st group : 1st alt:  1 to n spaces (greedy), 2nd alt: ^ asserts position at the start of the string, 3rd alt: char ; 4th alt: }
			#pattern explanation: 2nd group : matches word inline, matches 1 to n spaces (greedy) 
			if($noInline === true && (preg_match("/([[:space:]]+|^|;|})(inline)[[:space:]]+/", $retTypeFunName[1]) === 1))
				continue;

			#separates parameters, delimiter is , or )
			#pattern explanation: 1 to n a-Z0-9 chars or spaces or * or _ or [ or ] (greedy) and matches 1 char ) or , 
			preg_match_all("/[[:alnum:][:space:]*_\[\]]+[),]/", $fncs[0][$i],$params);
			$pC = 0;
			foreach ($params[0] as $param) {
				if((preg_match("/void/", $param)) !== 0)
					continue;
				else if($param = "")
					continue;
				else
					$pC++;	
			}
			if(($maxPar < $pC) && $maxParBool){
				continue;
			}
			
			if($noDups && (in_array($retTypeFunName[2], $dups)))
				continue;

			$dups[$dupsCount++] = $retTypeFunName[2];

			$fncEl = $xmlDoc->createElement('function');
			$fncEl->setAttribute('file',$oneFile);			
			$fncEl->setAttribute('name',$retTypeFunName[2]);
			$fncEl->setAttribute('varargs',$varArgs);
			if($removeWS){
				#remove spaces
				$retTypeFunName[1] = preg_replace("/[[:space:]]+/", ' ', $retTypeFunName[1]);
		        #pattern explanation: 0 to n spaces (greedy), 1 * char , 0 to n spaces (greedy)
		        #removes whitespace before and after *
		        $retTypeFunName[1] = preg_replace("/[[:space:]]*\*[[:space:]]*/", '*', $retTypeFunName[1]);
			}
			$retTypeFunName[1] = preg_replace("/\s+/", " ", $retTypeFunName[1]);

			$fncEl->setAttribute('rettype',$retTypeFunName[1]);
			$fncEl = $root->appendChild($fncEl);
				for($c = 0; $c < count($params[0]); $c++) {	
					#remove spaces from the start of the string
					$rez=preg_replace("/^[[:space:]]/","",$params[0][$c]);
					#pattern explanation: 0 to n spaces (greedy), 0 to n a-Z0-9 or _ (greedy), 0 to n spaces (greedy), 1 ) or , char
					$type=preg_replace("/[[:space:]]*[[:alnum:]_]*[[:space:]]*[),]/","",$rez);					
					if($type == NULL)
						continue;
					$paramEl = $xmlDoc->createElement('param');
					$paramEl->setAttribute('number',$c+1);
					if($removeWS){
						#remove spaces
						$type = preg_replace("/[[:space:]]+/", ' ', $type);
				        #pattern explanation: 0 to n spaces (greedy), 1 * char , 0 to n spaces (greedy)
				        #removes whitespace before and after *
		        		$type = preg_replace("/[[:space:]]*\*[[:space:]]*/", '*', $type);
					}
					$type = preg_replace("/\s+/", " ", $type);
					$paramEl->setAttribute('type',$type);
					$paramEl = $fncEl->appendChild($paramEl);			
				}
		}

	} else {
		foreach ($files as $filename) {
			$fncs = getContent($filename);	
			$pro = explode("/", $filename);
			$rfn = $pro[count($pro) - 1];
			for($i = 0;$i < count($fncs[0]); $i++) {
			# pattern explanation : one a-Z or _ char 0 to n alphanum or spaces 1 to n graphical chars 0 to n spaces 1 to n alphanum or _ chars 0 to n spaces and one ( char every quantifier expands as needed and works as few times as possible (lazy). Separates the input regexp to two groups first group is the return type of a function and the second group is the name of the function
			$noParamsPattern = "/([a-zA-Z_][[:alpha:]\s]*?[[:graph:]]+?)[\s]*?([[:alnum:]_]+?)[\s]*?\(/";
			preg_match($noParamsPattern, $fncs[0][$i], $retTypeFunName);
			
			#pattern explanation : 1 ( char, 0 to n characters except ^ and ) but as many times as possible, giving back as needed (greedy). Separates the function parameters from it's return type and identificator
			preg_match("/\([^\)]*/", $fncs[0][$i],$res);
			$allParams = substr($res[0], 1);
			$paramsSep = explode(",", $allParams);
			$varArgsPatt = "/\.\.\./u";			
			$isVar = preg_match($varArgsPatt, $fncs[0][$i]);
			if($isVar === 1) {
				$varArgs = "yes";
			} else {
				$varArgs = "no";
			}
			
			#removes inline functions
			#pattern explanation: 1st group : 1st alt:  1 to n spaces (greedy), 2nd alt: ^ asserts position at the start of the string, 3rd alt: char ; 4th alt: }
			#pattern explanation: 2nd group : matches word inline, matches 1 to n spaces (greedy) 			
			if($noInline === true && (preg_match("/([[:space:]]+|^|;|})(inline)[[:space:]]+/", $retTypeFunName[1]) === 1))
				continue;

			#separates parameters, delimiter is , or )
			#pattern explanation: 1 to n a-Z0-9 chars or spaces or * or _ or [ or ] (greedy) and matches 1 char ) or , 
			preg_match_all("/[[:alnum:][:space:]*_\[\]]+[),]/", $fncs[0][$i],$params);

			$pC = 0;
			foreach ($params[0] as $param) {
				if((preg_match("/void/", $param)) !== 0)
					continue;
				else if($param = "")
					continue;
				else
					$pC++;	
			}

			if(($maxPar > $pC) && $maxParBool){
				continue;
			}

			if($noDups && (in_array($retTypeFunName[2], $dups)))
				continue;

			$dups[$dupsCount++] = $retTypeFunName[2];

			$fncEl = $xmlDoc->createElement('function');
			$fncEl->setAttribute('file',$rfn);
			
			$fncEl->setAttribute('name',$retTypeFunName[2]);
			$fncEl->setAttribute('varargs',$varArgs);
			if($removeWS){
				$retTypeFunName[1] = preg_replace("/[[:space:]]+/", ' ', $retTypeFunName[1]);
		        #pattern explanation: 0 to n spaces (greedy), 1 * char , 0 to n spaces (greedy)
		        #removes whitespace before and after *
		        $retTypeFunName[1] = preg_replace("/[[:space:]]*\*[[:space:]]*/", '*', $retTypeFunName[1]);
			}
			$retTypeFunName[1] = preg_replace("/\s+/", " ", $retTypeFunName[1]);
			$fncEl->setAttribute('rettype',$retTypeFunName[1]);
			$fncEl = $root->appendChild($fncEl);
				for($c = 0; $c < count($params[0]); $c++) {	
					$rez=preg_replace("/^[[:space:]]/","",$params[0][$c]);
					#pattern explanation: 0 to n spaces (greedy), 0 to n a-Z0-9 or _ (greedy), 0 to n spaces (greedy), 1 ) or , char
					$type=preg_replace("/[[:space:]]*[[:alnum:]_]*[[:space:]]*[),]/","",$rez);					
					$paramEl = $xmlDoc->createElement('param');
					$paramEl->setAttribute('number',$c+1);
					if($removeWS){
						$type = preg_replace("/[[:space:]]+/", ' ', $type);
				        #pattern explanation: 0 to n spaces (greedy), 1 * char , 0 to n spaces (greedy)
				        #removes whitespace before and after *
		        		$type = preg_replace("/[[:space:]]*\*[[:space:]]*/", '*', $type);
					}	
					$type = preg_replace("/\s+/", " ", $type);
					$paramEl->setAttribute('type',$type);
					$paramEl = $fncEl->appendChild($paramEl);			
				}
			}

		}
	}
	$XmlFileText = preg_replace("/\n/", "", $xmlDoc->saveXML());

	if ($prettyXml) {
		#add spaces $offset times before the <function> element
		$XmlFileText = str_replace("<function ", str_repeat(" ", $offset) . "<function ", $XmlFileText);
		$aux = 0;
		#if somewhere's two newlines reduce them to just one
		$XmlFileText = str_replace("\n\n<f", "\n<f", $XmlFileText);
		$XmlFileText = str_replace("\n<?", "<?", $XmlFileText);
		#add spaces before the end of the function element 
		$XmlFileText = str_replace("</function>", str_repeat(" ", $offset) . "</function>", $XmlFileText);
		#sets offset times 2 spaces before every <param element
		$XmlFileText = str_replace("<param ", str_repeat(" ", $offset*2) . "<param ", $XmlFileText);
		#every element starts at a new line
		$XmlFileText = str_replace("<", "\n<", $XmlFileText);
	} else {
		$XmlFileText = str_replace("\n", "", $XmlFileText);
		$XmlFileText .= "\n";
	}

	if($output !== STDOUT)
		if(!($output = @fopen($output, "w"))){
			fprintf(STDERR, "Can't open output file.\n");
			exit(CANT_OPEN_OUTPUT);
		}
	fwrite($output, $XmlFileText);
?>