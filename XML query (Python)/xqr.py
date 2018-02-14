#!/usr/bin/python3.6
# -*- coding: UTF-8 -*-
#XQR:xgulaj00
#FIT VUT IPP proj 2

from xml.dom.minidom import parse
import xml.dom.minidom
import sys
import argparse
import re
class ArgParse:
	def __init__(self):
		outputFile = sys.stdout
		query = ''
		qf = ''
		root = ''
	if(len(sys.argv) > 1):
		if(sys.argv[1] == '--help'):
			sys.stdout.write("""Usage:
--help Prints help.
--input Input file with an XML structure.
--output Output file with filtered data.
--query Query on data.
--qf Query in a file.
-n Dont generate a header for our output XML.
--root Name of a root element to enclose the results.\n""")
			sys.exit(0)	
	else: sys.exit(1)
# defining the valid options of our argument parser
	inputFile = sys.stdin
	parser = argparse.ArgumentParser(add_help = False)
	parser.add_argument('--help', action="store_true", dest="help", help='Prints help.')
	parser.add_argument('--input', action="store", dest="inputFile", help='Input file with an XML structure.')
	parser.add_argument('--output', action="store", dest="outputFile", help='Output file with filtered data.')
	parser.add_argument('--query', help='Query on data.')
	parser.add_argument('--qf', action="store", dest="qf", help='Query in a file.')
	parser.add_argument('-n', action='store_true', dest="n", help='Dont generate a header for our output XML.')
	parser.add_argument('--root', action='store', dest="root", help='Name of a root element to enclose the results.')
	try:
		args = parser.parse_args()
	except:
		sys.stderr.write("Invalid arguments.\n")
		sys.exit(1)

	if(args.help != False) : 
		sys.stderr.write("Invalid arguments. Help has to be the only argument if given.\n")
		sys.exit(1)
# if no query was given at all call error
	if((args.query == None) and (args.qf == None)) :
		sys.stderr.write("Invalid arguments. Query or query file has to be other than none.\n")
		sys.exit(1)
#if both types of a query were given call error
	if((args.query != None) and (args.qf != None)) :
		sys.stderr.write("Invalid arguments. Query and query file cannot be true at the same time use only one of them.\n")
		sys.exit(1)
#if no input file was given call error
	if(args.inputFile != None):
		try:
			inFD = open(args.inputFile, "r")
		except:
			sys.stderr.write("Cannot open input file.\n")
			sys.exit(2)
#if no output file was  given call error
	if(args.outputFile != None):
		try:
			outFD = open(args.outputFile, "w")
		except:
			sys.stderr.write("Cannot open output file.\n")
			sys.exit(3)
#try to open query file
	if(args.qf != None):
		try:
			queryFD = open(args.qf, "r")
		except:
			sys.stderr.write("Cannot open query file.\n")
			sys.exit(11)

	ls = []
	inputFile = args.inputFile
	qr = args.query
	result = dict()
	if(args.query == None) :
		qr = queryFD.read()
#if theres someting in our query
	if(qr != None) :
		notFLAG = False
		limFLAG = False
		pattern = "SELECT\s+(\S+)\s+FROM\s+(\S+)\s?(WHERE\s+([NOT\s]+)?([\S\s]+))?\s?(LIMIT\s+(\S+))?"
		patt = re.compile(pattern)
		matched = re.match(patt, qr)
		try:
		  matched.group(0)
		except:
		  sys.stderr.write("Invalid query9.\n")
		  sys.exit(80)      
#separating the query
		result['select'] = matched.group(1)
		result['from'] = matched.group(2)		
		if(matched.group(6) != None) :
			limFLAG = True
		limit = matched.group(7)

		if(matched.group(4) != None) :
			result['not'] = matched.group(4)
			notLen = None
			notLen = result['not']
			notLen = notLen.split()
			notLen = len(notLen)
			if(notLen % 2 == 0) :
				notFLAG = False
			elif(notLen > 0) :
				notFLAG = True
		result['where'] = matched.group(5)
#initializing variables	
	s_CONTAINS = s_EQ = s_GR = s_LE = False
#not count starts at zero and increments for each not
	notCount = 0
	condition = result['from']
	partsOC = condition.split()
#initializing element and attribute in case they 
	element = None
	attribute = None
#if theres a dot we know its not only an element
	if(result['from'].find('.') != -1) :
		attribute = condition.split('.')
		realAtt = attribute[1]
		if(len(attribute[0]) != 0) :
			element = attribute[0]
		else :
			element = None
	else :
		realAtt = None
		element = result['from']
#if theres something followin the where clause
	if(result['where'] != None) :
		regExp = "([a-zA-Z_.]+)\s?([[=><])?(CONTAINS)?\s?(\S+)\s?((LIMIT)?\s(\S+)?)?"
		ptrn = re.compile(regExp)
		match = re.match(ptrn, result['where'])
		try:
		  match.group(0)
		except:
		  sys.stderr.write("Invalid query2.\n")
		  sys.exit(80)      
#saving the element in where		
		if(match.group(1) != None) :
			el = match.group(1)
		else :
			sys.stderr("Invalid query3.\n")
			sys.exit(80)
#separating the where clause string by string
		operator = match.group(2)
#regexping the WHERE clause
		contains = match.group(3)
		if(match.group(4) != None) :
			literal = match.group(4)
		else :
			sys.stderr("Invalid query5.\n")
			sys.exit(80)
#limitFlag tells us if theres LIMIT present in the query
		limFLAG = False
		if(match.group(6) != None) :
			limFLAG = True
		limit = match.group(7)
		if(limFLAG == True and limit == None) :
			sys.stderr("Invalid query6.\n")
			sys.exit(80)
		if (contains != None) :
			operator = "CONTAINS"

		if(el.find('.') != -1) :
			where_Attribute = el.split('.')
			where_RealAtt = where_Attribute[1]
			if(len(where_Attribute[0]) != 0) :
				where_Element = where_Attribute[0]
			else :
				where_Element = None
		else :
			where_RealAtt = None
			where_Element = el
		if(element == None and realAtt == None) :
			sys.stderr("Invalid query7.\n")
			sys.exit(80)
		if(where_Element == None and where_RealAtt == None) :
			sys.stderr("Invalid query8.\n")
			sys.exit(80)
		if(operator == None) :
			sys.stderr.write("Invalid query.\n")
			sys.exit(80)
	else :
		operator = literal = where_RealAtt = where_Element = None
#using minidom to format the output file	
	parsedFrom = None
	DOMTree = xml.dom.minidom.parse(inputFile)
	if(element == "ROOT") :
		parsedFrom = [DOMTree.documentElement]
	elif(element != None and realAtt == None) :
		parsedFrom = [DOMTree.getElementsByTagName(element)[0]]
	elif(element == None and realAtt != None) :
		allElems = DOMTree.getElementsByTagName("*")
		for elems in allElems :
			if(elems.hasAttribute(realAtt)) :
				parsedFrom = [elems]
				break
	elif(element != None and realAtt != None) :
		allElems = DOMTree.getElementsByTagName(element)
		for elems in allElems :
			if(elems.hasAttribute(realAtt)) :
				parsedFrom = [elems]
				break
	else :
		sys.stderr.write("Invalid query.\n")
		sys.exit(80)
	sel = result['select']
	rut = DOMTree.documentElement.tagName
	parsedSel = []
	if(sel == rut and element == "ROOT") :
		parsedSel = [DOMTree.documentElement]
	elif(sel != None) :
		for el in parsedFrom :
			parsedSel.extend(el.getElementsByTagName(sel))
	def conditionResult(nF, elemV, op, compV) :
		origin = compV
		compV = compV.replace("\"", "")
		if(op == ">") :
			if(nF == False) :
				return(elemV > compV)
			else :
				return(not(elemV > compV))
		elif(op == "<") :
			if(nF == False) :
				return float(elemV) < float(compV)
			else :
				return not(elemV < compV)
		elif(op == "=") :
			if(nF == False) :
				return(elemV == compV)
			else :
				return(not(elemV == compV))
		elif(op == "CONTAINS") :
			rExp = "\"\S+\""
			patrn = re.compile(rExp)
			match = re.match(patrn, origin)
			try :
				match.group(0)
			except :
				sys.stderr.write("Invalid query.\n")
				sys.exit(80)
			if(not(isinstance(compV, str))) :
				sys.stderr.write("Invalid query.\n")
				sys.exit(80)
			if(isinstance(elemV, str) and compV.replace("\"", "") in elemV) :
				if(nF == False) :
					return True
				else :
					return False
			else :
				if(nF == False) :
					return False
				else :
					return True
	parsedWhere = []
	if(result['where'] != None) :
		if(where_Element != None and where_RealAtt == None) :
			for elm in parsedSel :
				if (elm.tagName == where_Element):
					if (conditionResult(notFLAG, elm.firstChild.nodeValue, operator, literal)):
						parsedWhere.append(elm)
						continue
				for twoElem in elm.getElementsByTagName(where_Element):
					if (conditionResult(notFLAG ,twoElem.firstChild.nodeValue, operator, literal)):
						parsedWhere.append(elm)
						break
		elif(where_Element == None and where_RealAtt != None) :
			for elm in parsedSel :
				if(elm.hasAttribute(where_RealAtt) and conditionResult(notFLAG, elm.getAttribute(where_RealAtt), operator, literal) == True) :
					parsedWhere.append(elm)
					continue
				for elms in elm.getElementsByTagName("*") :
					if(elms.hasAttribute(where_RealAtt) and conditionResult(notFLAG, elms.getAttribute(where_RealAtt), operator, literal) == True) :
						parsedWhere.append(elm)
						break;
		elif(where_Element != None and where_RealAtt != None) :
			for elm in parsedSel :
				if (elm.tagName == where_Element) :
					if(elm.hasAttribute(where_RealAtt)) :
						if(conditionResult(notFLAG, elm.getAttribute(where_RealAtt), operator, literal)) :
							parsedWhere.append(elm)
							continue
					for twoElem in elm.getElementsByTagName(where_Element) :
						if(twoElem.hasAttribute(where_RealAtt)) :
							if(conditionResult(notFLAG, twoElem.getAttribute(where_RealAtt), operator, literal)) :	
								parsedWhere.append(elm)
								break
		elif(where_Element == None and where_RealAtt == None and result['where'] != None) :
			sys.stderr.write("Invalid query.\n")
			sys.exit(80)
# if n is false generate header
#	    print(s.attributes['name'].value)
#	functions = collection.getElementsByTagName(element)
#	attrs = functions.getElementsByTagName(co)
	if(args.n == False) :
		  outFD.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
	if(args.root != None) :
		outFD.write("<" + args.root + ">")
	limitCnt = 0
	if(limFLAG == True) :
		if(int(limit) < 0) :
			sys.stderr.write("Invalid query.\n")
			sys.exit(80)
	if(result['where'] == None) :
		for s in parsedSel:
			if(limFLAG == True) :
				if(limitCnt < int(limit)) :
					outFD.write(s.toxml())
					limitCnt += 1
			else :
				outFD.write(s.toxml())
	else :
		for s in parsedWhere:
			if(limFLAG == True) :
				if(limitCnt < int(limit)) :
					outFD.write(s.toxml())
					limitCnt += 1
			else :
				outFD.write(s.toxml())
#if root is present write root into the output file
	if(args.root != None) :
		outFD.write("</" + args.root + ">")
#if query file is present close it
	if(args.qf != None):
		queryFD.close()
#if input file is present close it
	if(args.inputFile != None):
		inFD.close()
#if input file is present close it
	if(args.outputFile != None):
		outFD.close()

sys.exit(0)
