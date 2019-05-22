<?php
	require_once("curl.php");
	
	class isas{
		private $server;
		private $cookies;
		private $curl;
		public $logintry = 1;
		
		public function __construct($server, $name = null, $pass = null){
			$this->server = $server;
			$this->cookies = tmpfile();
			$this->curl = new curl(stream_get_meta_data($this->cookies)["uri"]);
			
			if ($name && $pass) {
				return $this->login($name, $pass);
			}
		}
		
		public function login($name, $pass){
			
			$i = 0;
			
			do {
			
				$url = $this->server . "/prihlasit.php";
			
				$post = array(
					'login-isas-username' => urlencode($name),
					'login-isas-password' => urlencode($pass),
					'login-isas-send' => "isas-send"
				);
			
				$this->curl->post($url, $post);
				
				$i++;
				
			} while (!$this->checkLogin() && $i < $this->logintry);
			
			return $i < $this->logintry;
		}
		
		public function TableToArray($element, $type = "marks", $finder = null) {
    		$row = $element->getElementsByTagName('tr');
    		
    		$marks = array();
    		$indexs = array();
    		
    		if ($type == "marks"){
    			$header = $row[0];
    			
    			$needs = array (
					"date" => "Datum", 
					"subject" => "Předmět", 
					"mark" => "Známka", 
					"value" => "Hodnota", 
					"type" => "Typ zkoušení", 
					"weight" => "váha",
					"note" => "Poznámka",
					"teacher" => "Učitel"
				);
    		
    			foreach ($needs as $keyword => $need){
    				foreach ($header->getElementsByTagName("td") as $key => $td) {
    					if ($this->textClean($this->DOMinnerHTML($td)) == $this->textClean($need)){
    						$indexs[$key] = $keyword;
    					}
    				}
    			}
    		
    			for ($i = 1; $i < $row->length; $i++){
    				
    				$cells = $row->item($i)->getElementsByTagName("td");
    			
    				array_push($marks, array());
    			
    				foreach ($indexs as $key => $index){
    					$marks[$i - 1][$index] = html_entity_decode(strip_tags($this->DOMinnerHTML($cells->item($key))));
    				
    					if ($index == "date") {
    						$marks[$i - 1]["id"] = $this->GetIdFromLink($cells->item($key)->getElementsByTagName("a")->item(0)->getAttribute('href'));
    					}
    				}
    			}
    		} else if ($type == "markinfo") {
    			
    			$needs = array (
					"date" => "Datum", 
					"subject" => "Předmět", 
					"mark" => "Známka",
					"value" => "Hodnota Známky",
					"value" => "Hodnota", 
					"type" => "Typ zkoušení",
					"weight" => "váha",
					"teacher" => "Učitel"
				);
    			
    			foreach ($needs as $keyword => $need){
    				foreach ($row as $key => $tr) {
    					if ($this->textClean($this->DOMinnerHTML($tr->getElementsByTagName("td")[0])) == $this->textClean($need)){
    						$indexs[$key] = $keyword;
    					}
    				}
    			}
    		
    			foreach ($indexs as $key => $index){
    				$marks[$index] = html_entity_decode(strip_tags($this->DOMinnerHTML($row[$key]->getElementsByTagName("td")->item(1))));
    			}
    			
    			//get note
    			$note = $finder->query("//*[contains(@class, 'isas-zapisnik')]", $element);
    			$marks["note"] = html_entity_decode(strip_tags($this->DOMinnerHTML($note[0])));
    			
    		} else if ($type == "classmarks") {
    			
    			for ($i = 1; $i < $row->length; $i++){
    				$td = $row->item($i)->getElementsByTagName("td");
    				$marks[strip_tags($this->DOMinnerHTML($td->item(0)))] = html_entity_decode(strip_tags($this->DOMinnerHTML($td->item(2))));
    			}
    		} else if ($type == "userinfo") {
    			$needs = array (
					"user" => "Uživatel:", 
					"fullname" => "Jméno:", 
					"type" => "Typ:", 
					"lastlogin" => "Poslední přihlášení:"
				);
    			
    			foreach ($needs as $keyword => $need){
    				foreach ($row as $key => $tr) {
    					if ($this->textClean($this->DOMinnerHTML($tr->getElementsByTagName("td")[0])) == $this->textClean($need)){
    						$indexs[$key] = $keyword;
    					}
    				}
    			}
    		
    			foreach ($indexs as $key => $index){
    				$marks[$index] = html_entity_decode(strip_tags($this->DOMinnerHTML($row[$key]->getElementsByTagName("td")->item(1))));
    			}
    		}
    		
    		return $marks;
		}
		
		public function getUserInfo(){
			$url = $this->server . "/profil.php";
			$html = $this->curl->get($url);
			
			$dom = new DOMDocument();
			$dom->loadHTML($html);
			
			$finder = new DomXPath($dom);
    		$table = $finder->query("//*[contains(@class, 'isas-tabulka')]");
			
			return $this->TableToArray($table[0], "userinfo");
		}
		
		public function getUserClass(){
			$url = $this->server . "/prubeh-studia.php";
			$html = $this->curl->get($url);
			
			$dom = new DOMDocument();
			$dom->loadHTML($html);
			
			$finder = new DomXPath($dom);
    		$table = $finder->query("//*[contains(@class, 'isas-tabulka')]");
    		$row = $table[0]->getElementsByTagName('tr');
			$header = $row[0];
			
			$need = "Třída";
    			
    		foreach ($header->getElementsByTagName("td") as $key => $td) {
    			if ($this->textClean($this->DOMinnerHTML($td)) == $this->textClean($need)){
    				$index = $key;
    				break;
    			}
    		}
    		
    		$cells = $row->item($row->length - 1)->getElementsByTagName("td");
    		return strip_tags($this->DOMinnerHTML($cells->item($key)));
    	}
    	
    	public function getClassId($name){
    		$url = $this->server . "/rozvrh-hodin.php";
			$html = $this->curl->get($url);
			
			if ($this->curl->getCode() == 404){
				return false;
			} else {
				$dom = new DOMDocument();
				$dom->loadHTML($html);
			
				$finder = new DomXPath($dom);
    			$table = $finder->query("//*[contains(@class, 'isas-tabulka')]");
    			$row = $table[0]->getElementsByTagName('tr');
    			
    			$return = false;
    			foreach ($row as $class) {
    				if ($this->textClean(strip_tags($this->DOMinnerHTML($class))) == $this->textClean($name)){
    					$link = $class->getElementsByTagName("a")[0]->getAttribute("href");
    					$link = explode("&rozvrh=", $link);
						$return = intval($link[1]);
    					break;
    				}
    			}
    			return $return;
			}
    	}
    	
    	public function getSchedule($classid){
    		$url = $this->server . "/rozvrh-hodin.php?rozvrh=" . $classid;
			$html = $this->curl->get($url);
			
			if ($this->curl->getCode() == 404){
				return false;
			} else {
				$dom = new DOMDocument();
				$dom->loadHTML($html);
			
				$finder = new DomXPath($dom);
    			$table = $finder->query("//*[contains(@class, 'rozvrh-hodin')]");
    			$rowsud = $table[0]->getElementsByTagName('tr');
    			$rowlich = $table[1]->getElementsByTagName('tr');
    			
    			$need = array(
    				"monday" => "po",
    				"tuesday" => "ut",
    				"wednesday" => "st",
    				"thursday" => "čt",
    				"friday" => "pá"
    			);
    			
    			$daylength = array();
    			
    			for ($i = 2; $i < $rowsud->length; $i++){
    				foreach ($need as $key => $keyword){
    					if ($this->textClean($keyword) == $this->textClean(strip_tags($this->DOMinnerHTML($rowsud[$i]->getElementsByTagName("td")[0])))){
    						$daylength[$key] = intval($rowsud[$i]->getElementsByTagName("td")[0]->getAttribute("rowspan"));
    					}
    				}
    			}
    			
    			return $daylength;
    			//TODO: získání rozvrhu
			}
    	}
		
		public function logout(){
			$url = $this->server . "/odhlasit.php";
			$this->curl->get($url);
			//Remove TMP cookies file
			fclose($this->cookies);
		}
		
		public function GetMarkInfo($id){
			$url = $this->server . "/prubezna-klasifikace.php?zaznam=" . $id;
			$html = $this->curl->get($url);
			
			$return = array(
				"info" => array(),
				"classmarks" => array()
			);
			
			$dom = new DOMDocument();
			$dom->loadHTML($html);
			
			$finder = new DomXPath($dom);
    		$table = $finder->query("//*[contains(@class, 'isas-karta-polozky')]");
			
			$return['info'] = $this->TableToArray($table[0], "markinfo", $finder);
			
			$table = $finder->query("//*[contains(@class, 'isas-histogram')]");
			
			if ($table[0] <> null) {
				$return['classmarks'] = $this->TableToArray($table[0], "classmarks");
			}
			
			return $return;
		}
		
		public function GetIdFromLink($link){
			$link = explode("&zaznam=", $link);
			return intval($link[1]);
		}
		
		public function DOMinnerHTML($element) { 
    		$innerHTML = ""; 
    		$children = $element->childNodes; 
    		
    		foreach ($children as $child) { 
        		$tmp_dom = new DOMDocument(); 
        		$tmp_dom->appendChild($tmp_dom->importNode($child, true)); 
        		$innerHTML.=trim($tmp_dom->saveHTML()); 
    		} 
    		
    		return $this->fixCharset($innerHTML); 
		}
		
		public function textClean($string) {
			$string = $this->fixCharset($string);
			$string = str_replace(' ', '', html_entity_decode($string));
			return strtolower(preg_replace('/[^A-Za-z0-9\-]/', '', $string));
		}
		
		public function fixCharset($string){
			return iconv("Windows-1250", "UTF-8", $string);
		}
		
		public function getMarks(){
			$url = $this->server . "/prubezna-klasifikace.php";
			$html = $this->curl->get($url);
			
			$dom = new DOMDocument();
			$dom->loadHTML($html);
			
			$finder = new DomXPath($dom);
    		$table = $finder->query("//*[contains(@class, 'isas-tabulka')]");
			
			return $this->TableToArray($table[0], "marks");
		}
		
		public function checkLogin(){
			$url = $this->server . "/profil.php";
			$html = $this->curl->get($url);
			
			$dom = new DOMDocument();
			$dom->loadHTML($html);
			
			$finder = new DomXPath($dom);
    		$logoutbars = $finder->query("//*[contains(@class, 'pravy-blok')]");
    		
    		$login = false;
    		
    		foreach ($logoutbars as $logoutbar){
    			$links = $logoutbar->getElementsByTagName("a");
    		
    			foreach ($links as $link){
    				if ($link->getAttribute("href") == "odhlasit.php") {
    					$login = true;
    					break;
    				}
    			}
    		}
    		
    		return $login;
		}
	}
?>